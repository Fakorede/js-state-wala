import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try {
            Path path = Paths.get(args[0]);
            String fileName = path.getParent().toString() + "/" + path.getFileName().toString();

            printIRs(fileName);
        } catch (Exception e) {
            System.out.println("Something went wrong");
            System.out.println(e.getMessage());
            // e.printStackTrace();
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
                if (m != null && !m.getSignature().equals("index.js.do()LRoot;")) {
                    IR ir = factory.makeIR(m, Everywhere.EVERYWHERE,
                            new SSAOptions());

                    System.out.println("\nAccess paths for method: " + m);
                    System.out.println(getModifiedInstanceVariables(ir));
                }
            }
        }
    }

    /**
     * Checks for lexical writes in
     * @param ir
     * @return Set<String>
     */
    public static Set<String> getModifiedInstanceVariables(IR ir) {
        Set<String> mod = new HashSet<>();

        for (int i = 0; i < ir.getInstructions().length; i++) {
            SSAInstruction inst = ir.getInstructions()[i];

            if (inst != null && inst.getClass().equals(AstLexicalWrite.class)) {
                for (int v = 0; v < inst.getNumberOfUses(); v++) {
                    String[] names = ir.getLocalNames(i, inst.getUse(v));

                    if (names != null && names.length != 0) {
                        mod.add(names[0]);
                    }
                }
            }
        }

        return mod;
    }
}
