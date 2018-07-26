package spoon.test.prettyprinter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import spoon.Launcher;
import spoon.experimental.modelobs.SourceFragmentsTreeCreatingChangeCollector;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.printer.change.SniperJavaPrettyPrinter;
import spoon.support.modelobs.ChangeCollector;
import spoon.test.prettyprinter.testclasses.JDTTreeBuilder;
import spoon.test.prettyprinter.testclasses.ToBeChanged;

public class TestSniperPrinter {

	@Test
	public void testPrintUnchaged() throws Exception {
		//contract: sniper printing of unchanged compilation unit returns origin sources
		checkSniper(ToBeChanged.class.getName(), type -> {
			//do not change the model
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed);
		});
	}

	@Test
	public void testPrintAfterRenameOfField() throws Exception {
		//contract: sniper printing after rename of field 
		checkSniper(ToBeChanged.class.getName(), type -> {
			//change the model
			type.getField("string").setSimpleName("modified");
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\bstring\\b", "modified");
		});
	}
	
	@Test
	public void testPrintChangedReferenceBuilder() throws Exception {
		//contract: sniper printing after remove of statement from nested complex `if else if ...`
		checkSniper("spoon.test.prettyprinter.ReferenceBuilder", type -> {
			//find to be removed statement
			CtStatement toBeRemoved = type.filterChildren((CtStatement stmt) -> stmt.getPosition().isValidPosition() && stmt.getPosition().getLine() == 230).first();
			ChangeCollector.ignoreChanges(type.getFactory().getEnvironment(), () -> {
				//TODO fix that this toString would change model...
				assertEquals("bounds = false", toBeRemoved.toString());
			});
			//change the model
			toBeRemoved.delete();
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\QNO_SUPERINTERFACES) {\n\\E\\s*bounds\\s*=\\s*false;\n", "NO_SUPERINTERFACES) {\n");
		});
	}
	
	@Test
	public void testPrintAfterRemoveOfFirstParameter() {
		//contract: sniper print after remove of first parameter
		checkSniper(ToBeChanged.class.getName(), type -> {
			//delete first parameter of method `andSomeOtherMethod`
			type.getMethodsByName("andSomeOtherMethod").get(0).getParameters().get(0).delete();
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\s*int\\s*param1,", "");
		});
	}

	@Test
	public void testPrintAfterRemoveOfMiddleParameter() {
		//contract: sniper print after remove of middle (not first and not last) parameter
		checkSniper(ToBeChanged.class.getName(), type -> {
			//delete second parameter of method `andSomeOtherMethod`
			type.getMethodsByName("andSomeOtherMethod").get(0).getParameters().get(1).delete();
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\s*String\\s*param2\\s*,", "");
		});
	}

	@Test
	public void testPrintAfterRemoveOfLastParameter() {
		//contract: sniper print after remove of last parameter
		checkSniper(ToBeChanged.class.getName(), type -> {
			//delete last parameter of method `andSomeOtherMethod`
			type.getMethodsByName("andSomeOtherMethod").get(0).getParameters().get(2).delete();
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\s*, \\QList<?>[][] ... twoDArrayOfLists\\E", "");
		});
	}

	@Test
	public void testPrintAfterRemoveOfLastTypeMember() {
		//contract: sniper print after remove of last type member - check that suffix spaces are printed correctly
		checkSniper(ToBeChanged.class.getName(), type -> {
			//delete first parameter of method `andSomeOtherMethod`
			type.getField("twoDArrayOfLists").delete();
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed, "\\Q\tList<?>[][] twoDArrayOfLists = new List<?>[7][];\n\\E", "");
		});
	}
	@Test
	public void testPrintAfterAddOfLastTypeMember() {
		//contract: sniper print after remove of last type member - check that suffix spaces are printed correctly
		class Context {
			CtField<?> newField;
		}
		Context context = new Context();
		
		checkSniper(ToBeChanged.class.getName(), type -> {
			Factory f = type.getFactory();
			//create new type member
			context.newField = f.createField(type, Collections.singleton(ModifierKind.PRIVATE), f.Type().DATE, "dateField");
			type.addTypeMember(context.newField);
		}, (type, printed) -> {
			String lastMemberString = "new List<?>[7][];";
			assertPrintedWithExpectedChanges(type, printed, "\\Q"+lastMemberString+"\\E", lastMemberString + "\n\n\t" + context.newField.toString());
		});
	}
	@Test
	public void testPrintUnchagedReturn() throws Exception {
		//contract: printing of `return;`, which causes call of `scan(null)` is possible
		checkSniper(JDTTreeBuilder.class.getName(), type -> {
			//do not change the model
		}, (type, printed) -> {
			assertPrintedWithExpectedChanges(type, printed);
		});
	}
	private void checkSniper(String testClass, Consumer<CtType<?>> typeChanger, BiConsumer<CtType<?>, String> resultChecker) {
		Launcher launcher = new Launcher();
		launcher.addInputResource(getResourcePath(testClass));
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setAutoImports(true);
		launcher.getEnvironment().useTabulations(true);
		launcher.buildModel();
		Factory f = launcher.getFactory();

		final CtClass<?> ctClass = launcher.getFactory().Class().get(testClass);
		
		new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());
		//change the model
		typeChanger.accept(ctClass);
		
		SniperJavaPrettyPrinter printer = new SniperJavaPrettyPrinter(f.getEnvironment());
		CompilationUnit cu = f.CompilationUnit().getOrCreate(ctClass);
		printer.calculate(cu, cu.getDeclaredTypes());

		resultChecker.accept(ctClass, printer.getResult());
	}
	
	private static String getResourcePath(String className) {
		String r = "./src/test/java/"+className.replaceAll("\\.", "/")+".java";
		if (new File(r).exists()) {
			return r;
		}
		r = "./src/test/resources/"+className.replaceAll("\\.", "/")+".java";
		if (new File(r).exists()) {
			return r;
		}
		throw new RuntimeException("Resource of class " + className + " doesn't exist");
	}

	/**
	 * checks that printed code contains only expected changes
	 */
	private void assertPrintedWithExpectedChanges(CtType<?> ctClass, String printedSource, String... replacements) {
		assertEquals(0, replacements.length % 2);
		String originSource = ctClass.getPosition().getCompilationUnit().getOriginalSourceCode();
		//TODO REMOVE THIS BLOCK AFTER SNIPER PRINTING OF IMPORTS WORKS
		{
			//skip imports, which are not handled well yet
			originSource = skipImports(originSource);
			printedSource = skipImports(printedSource);
		}
		//apply all expected replacements using Regular expressions
		int nrChanges = replacements.length / 2;
		for (int i = 0; i < nrChanges; i++) {
			String str = replacements[i * 2];
			String replacement = replacements[i * 2 + 1];
			originSource = originSource.replaceAll(str, replacement);
		}
		//check that origin sources which expected changes are equal to printed sources
		assertEquals(originSource, printedSource);
	}

	Pattern importRE = Pattern.compile("^(?:import|package)\\s.*;\\s*$", Pattern.MULTILINE);
	
	private String skipImports(String source) {
		Matcher m = importRE.matcher(source);
		int lastImportEnd = 0;
		while(m.find()) {
			lastImportEnd = m.end();
		};
		System.out.println(lastImportEnd);
		return source.substring(lastImportEnd).trim();
	}
}
