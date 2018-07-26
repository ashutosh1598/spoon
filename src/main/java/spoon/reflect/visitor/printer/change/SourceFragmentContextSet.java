package spoon.reflect.visitor.printer.change;

import java.util.List;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

/**
 * Handles printing of changes of the unordered Set of elements.
 * E.g. set of modifiers and annotations or set of super interfaces
 * Such sets must be printed in same order like they were in origin source code
 *
 * If anything is modified (add/remove/modify element) in such collection,
 * then collection is printed in the order defined by {@link DefaultJavaPrettyPrinter}.
 * The not modified items of collection are printed using origin sources
 */
class SourceFragmentContextSet extends AbstractSourceFragmentContextCollection {
	/**
	 * @param mutableTokenWriter {@link MutableTokenWriter}, which is used for printing
	 * @param element the {@link CtElement} whose list attribute is handled
	 * @param collectionFragment the {@link CollectionSourceFragment}, which represents whole list of elements. E.g. body of method or all type members of type
	 */
	SourceFragmentContextSet(MutableTokenWriter mutableTokenWriter, CtElement element, List<SourceFragment> fragments, ChangeResolver changeResolver) {
		super(mutableTokenWriter, fragments, changeResolver);
	}

	@Override
	public void onPrintEvent(PrinterEvent event) {
		// TODO Auto-generated method stub
		super.onPrintEvent(event);
	}
}
