package com.ibm.wala.examples;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.examples.drivers.ConstructAllIRs;
import com.ibm.wala.examples.drivers.JSCallGraphDriver;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        try {
            Path path = Paths.get(args[0]);
            String fileName = path.getParent().toString() + "/" + path.getFileName().toString();

            System.out.println("Analyzing js code for variables...");
            printIRs(fileName);
        } catch (Exception e) {
            System.out.println("Something went wrong");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    public static void printIRs(String filename) throws ClassHierarchyException {
        // use Rhino to parse JavaScript
        JSCallGraphUtil.setTranslatorFactory(
                new CAstRhinoTranslatorFactory());
        // build a class hierarchy, for access to code info
        IClassHierarchy cha =
                JSCallGraphUtil.makeHierarchyForScripts(filename);
        // for constructing IRs
        IRFactory<IMethod> factory = AstIRFactory.makeDefaultFactory();
        for (IClass klass : cha) {
            // ignore models of built-in JavaScript methods
            if (!klass.getName().toString().startsWith("prologue.js")) {
                // get the IMethod representing the code (the ‘do’ method)
                IMethod m = klass.getMethod(AstMethodReference.fnSelector);
                if (m != null) {
                    IR ir = factory.makeIR(m, Everywhere.EVERYWHERE,
                            new SSAOptions());

                    for (SSAInstruction instruction : ir.getInstructions()) {
                        if (instruction == null) {
                            continue;
                        }

                        String variableClass = "class com.ibm.wala.cast.ir.ssa.AstGlobalRead";

                        if (instruction.getClass().toString().equals(variableClass)) {
                            var position = instruction.getDef();
                            var variableName = ir.getLocalNames(position, position);

                            if (variableName.length != 0) {
                                System.out.println(ir.getLocalNames(position, position)[0]);
                            }
                        }
                    }
                }
            }
        }
    }
}
