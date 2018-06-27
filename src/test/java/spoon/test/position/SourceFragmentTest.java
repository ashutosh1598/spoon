package spoon.test.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.printer.change.FragmentType;
import spoon.reflect.visitor.printer.change.SourceFragment;
import spoon.support.reflect.cu.position.BodyHolderSourcePositionImpl;
import spoon.support.reflect.cu.position.DeclarationSourcePositionImpl;
import spoon.support.reflect.cu.position.SourcePositionImpl;
import spoon.test.position.testclasses.MethodWithJavaDocAndModifiers;

public class SourceFragmentTest {

	@Test
	public void testSourcePositionFragment() throws Exception {
		SourcePosition sp = new SourcePositionImpl(null, 10, 20, null);
		SourceFragment sf = new SourceFragment(sp);
		assertEquals(10, sf.getStart());
		assertEquals(21, sf.getEnd());
		assertSame(sp, sf.getSourcePosition());
		assertNull(sf.getFirstChild());
		assertNull(sf.getNextSibling());

	}
	@Test
	public void testDeclarationSourcePositionFragment() throws Exception {
		SourcePosition sp = new DeclarationSourcePositionImpl(null, 100, 110, 90, 95, 90, 130, null);
		SourceFragment sf = new SourceFragment(sp);
		assertEquals(90, sf.getStart());
		assertEquals(131, sf.getEnd());
		assertSame(sp, sf.getSourcePosition());

		assertNotNull(sf.getFirstChild());
		assertNull(sf.getNextSibling());
		
		SourceFragment sibling;
		sibling = sf.getFirstChild();
		assertSame(FragmentType.MODIFIERS, sibling.getFragmentType());
		assertEquals(90, sibling.getStart());
		assertEquals(96, sibling.getEnd());
		
		sibling = sibling.getNextSibling();
		assertSame(FragmentType.BEFORE_NAME, sibling.getFragmentType());
		assertEquals(96, sibling.getStart());
		assertEquals(100, sibling.getEnd());
		
		sibling = sibling.getNextSibling();
		assertSame(FragmentType.NAME, sibling.getFragmentType());
		assertEquals(100, sibling.getStart());
		assertEquals(111, sibling.getEnd());

		sibling = sibling.getNextSibling();
		assertSame(FragmentType.AFTER_NAME, sibling.getFragmentType());
		assertEquals(111, sibling.getStart());
		assertEquals(131, sibling.getEnd());
	}
	
	@Test
	public void testBodyHolderSourcePositionFragment() throws Exception {
		SourcePosition sp = new BodyHolderSourcePositionImpl(null, 100, 110, 90, 95, 90, 130, 120, 130, null);
		SourceFragment sf = new SourceFragment(sp);
		assertEquals(90, sf.getStart());
		assertEquals(131, sf.getEnd());
		assertSame(sp, sf.getSourcePosition());

		assertNotNull(sf.getFirstChild());
		assertNull(sf.getNextSibling());
		
		SourceFragment sibling;
		sibling = sf.getFirstChild();
		assertSame(FragmentType.MODIFIERS, sibling.getFragmentType());
		assertEquals(90, sibling.getStart());
		assertEquals(96, sibling.getEnd());
		
		sibling = sibling.getNextSibling();
		assertSame(FragmentType.BEFORE_NAME, sibling.getFragmentType());
		assertEquals(96, sibling.getStart());
		assertEquals(100, sibling.getEnd());
		
		sibling = sibling.getNextSibling();
		assertSame(FragmentType.NAME, sibling.getFragmentType());
		assertEquals(100, sibling.getStart());
		assertEquals(111, sibling.getEnd());

		sibling = sibling.getNextSibling();
		assertSame(FragmentType.AFTER_NAME, sibling.getFragmentType());
		assertEquals(111, sibling.getStart());
		assertEquals(120, sibling.getEnd());

		sibling = sibling.getNextSibling();
		assertSame(FragmentType.BODY, sibling.getFragmentType());
		assertEquals(120, sibling.getStart());
		assertEquals(131, sibling.getEnd());
	}
	
	@Test
	public void testSourceFragmentAddChild() throws Exception {
		//contract: check build of the tree of SourceFragments
		SourceFragment rootFragment = createFragment(10, 20);
		SourceFragment f;
		//add child
		assertSame(rootFragment, rootFragment.add(f = createFragment(10, 15)));
		assertSame(rootFragment.getFirstChild(), f);
		
		//add child which is next sibling of first child
		assertSame(rootFragment, rootFragment.add(f = createFragment(15, 20)));
		assertSame(rootFragment.getFirstChild().getNextSibling(), f);
		
		//add another child of same start/end, which has to be child of last child
		assertSame(rootFragment, rootFragment.add(f = createFragment(15, 20)));
		assertSame(rootFragment.getFirstChild().getNextSibling().getFirstChild(), f);

		//add another child of smaller start/end, which has to be child of last child
		assertSame(rootFragment, rootFragment.add(f = createFragment(16, 20)));
		assertSame(rootFragment.getFirstChild().getNextSibling().getFirstChild().getFirstChild(), f);

		//add next sibling of root element
		assertSame(rootFragment, rootFragment.add(f = createFragment(20, 100)));
		assertSame(rootFragment.getNextSibling(), f);
		
		//add prev sibling of root element. We should get new root
		f = createFragment(5, 10);
		assertSame(f, rootFragment.add(f));
		assertSame(f.getNextSibling(), rootFragment);
	}
	
	@Test
	public void testSourceFragmentWrapChild() throws Exception {
		//contract: the existing child fragment can be wrapped by a new parent 
		SourceFragment rootFragment = createFragment(0, 100);
		SourceFragment child = createFragment(50, 60);
		rootFragment.add(child);
		
		SourceFragment childWrapper = createFragment(40, 60);
		rootFragment.add(childWrapper);
		assertSame(rootFragment.getFirstChild(), childWrapper);
		assertSame(rootFragment.getFirstChild().getFirstChild(), child);
	}
	@Test
	public void testSourceFragmentWrapChildrenAndSiblings() throws Exception {
		//contract: the two SourceFragment trees merge correctly together 
		SourceFragment rootFragment = createFragment(0, 100);
		SourceFragment child = createFragment(50, 60);
		rootFragment.add(child);
		
		SourceFragment childWrapper = createFragment(40, 70);
		SourceFragment childA = createFragment(40, 50);
		SourceFragment childB = createFragment(50, 55);
		SourceFragment childC = createFragment(60, 65);
		SourceFragment childD = createFragment(65, 70);
		//add all children and check the root is still childWrapper
		assertSame(childWrapper, childWrapper.add(childA).add(childB).add(childC).add(childD));
		
		rootFragment.add(childWrapper);
		assertSame(rootFragment.getFirstChild(), childWrapper);
		assertSame(childA, childWrapper.getFirstChild());
		assertSame(child, childA.getNextSibling());
		assertSame(childB, child.getFirstChild());
		assertSame(childC, child.getNextSibling());
		assertSame(childD, childC.getNextSibling());
	}

	@Test
	public void testLocalizationOfSourceFragment() throws Exception {
		SourceFragment rootFragment = createFragment(0, 100);
		SourceFragment x;
		rootFragment.add(createFragment(50, 60));
		rootFragment.add(createFragment(60, 70));
		rootFragment.add(x = createFragment(50, 55));
		
		assertSame(x, rootFragment.getSourceFragmentOf(50, 55));
		assertSame(rootFragment, rootFragment.getSourceFragmentOf(0, 100));
		assertSame(rootFragment.getFirstChild(), rootFragment.getSourceFragmentOf(50, 60));
		assertSame(rootFragment.getFirstChild().getNextSibling(), rootFragment.getSourceFragmentOf(60, 70));
	}
	
	
	private SourceFragment createFragment(int start, int end) {
		return new SourceFragment(new SourcePositionImpl(null, start, end - 1, null));
	}
	
	@Test
	public void testSourceFragmentOfMethodWithComment() throws Exception {
		final Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(false);
		launcher.getEnvironment().setCommentEnabled(true);
		SpoonModelBuilder comp = launcher.createCompiler();
		comp.addInputSources(SpoonResourceHelper.resources("./src/test/java/" + MethodWithJavaDocAndModifiers.class.getName().replace('.', '/') + ".java"));
		comp.build();
		Factory f = comp.getFactory();
		
		final CtType<?> foo = f.Type().get(MethodWithJavaDocAndModifiers.class);

		CtMethod method2 = foo.getMethodsByName("mWithDoc").get(0);
		CompilationUnit cu = foo.getPosition().getCompilationUnit();
		SourceFragment fragment = cu.getSourceFragment(method2);
		//keep \n here because it reflects EOL of the file MethodWithJavaDocAndModifiers.java
		assertEquals("/**\n" + 
				"	 * Method with javadoc\n" + 
				"	 * @param parm1 the parameter\n" + 
				"	 */\n" + 
				"	public @Deprecated int mWithDoc(int parm1) {\n" + 
				"		return parm1;\n" + 
				"	}", fragment.getSourceCode());
		
		SourceFragment child = fragment.getFirstChild();
		assertSame(FragmentType.MODIFIERS, child.getFragmentType());
		assertEquals("/**\n" + 
				"	 * Method with javadoc\n" + 
				"	 * @param parm1 the parameter\n" + 
				"	 */\n" + 
				"	public @Deprecated", child.getSourceCode());
		{ //check children fragments of current `child`
			assertEquals("/**\n" + 
					"	 * Method with javadoc\n" + 
					"	 * @param parm1 the parameter\n" + 
					"	 */", child.getFirstChild().getSourceCode()); 
			assertEquals("@Deprecated", child.getFirstChild().getNextSibling().getSourceCode());
		}
		
		child = child.getNextSibling();
		assertSame(FragmentType.BEFORE_NAME, child.getFragmentType());
		//it is including spaces, because it represents spaces after modifiers and before name
		assertEquals(" int ", child.getSourceCode());
		{ //check children fragments of current `child`
			assertEquals("int", child.getFirstChild().getSourceCode()); 
		}
		
		child = child.getNextSibling();
		assertSame(FragmentType.NAME, child.getFragmentType());
		assertEquals("mWithDoc", child.getSourceCode());
		{ //check children fragments of current `child`
			assertNull(child.getFirstChild()); 
		}
		
		child = child.getNextSibling();
		assertSame(FragmentType.AFTER_NAME, child.getFragmentType());
		//it is including spaces, because it represents code after name and before body
		assertEquals("(int parm1) ", child.getSourceCode());
		{ //check children fragments of current `child`
			assertEquals("int parm1", child.getFirstChild().getSourceCode()); 
		}

		child = child.getNextSibling();
		assertSame(FragmentType.BODY, child.getFragmentType());
		assertEquals("{\n" + 
				"		return parm1;\n" + 
				"	}", child.getSourceCode());
		{ //check children fragments of current `child`
			assertEquals("{\n" + 
					"		return parm1;\n" + 
					"	}", child.getFirstChild().getSourceCode()); 
			assertEquals("return parm1;", child.getFirstChild().getFirstChild().getSourceCode()); 
		}
	}
	
}
