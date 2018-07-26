package spoon.reflect.visitor.printer.change;

import java.util.List;

import spoon.reflect.declaration.CtElement;

import static spoon.reflect.visitor.printer.change.ElementSourceFragment.isSpaceFragment;

/**
 * Handles printing of changes of the ordered list of elements.
 * E.g. list of type members of type
 * Such lists must be printed in same order like they are in List.
 */
class SourceFragmentContextList extends AbstractSourceFragmentContextCollection {
	/**
	 * @param mutableTokenWriter {@link MutableTokenWriter}, which is used for printing
	 * @param element the {@link CtElement} whose list attribute is handled
	 * @param collectionFragment the {@link CollectionSourceFragment}, which represents whole list of elements. E.g. body of method or all type members of type
	 */
	SourceFragmentContextList(MutableTokenWriter mutableTokenWriter, CtElement element, List<SourceFragment> fragments, ChangeResolver changeResolver) {
		super(mutableTokenWriter, fragments, changeResolver);
	}

	@Override
	protected int findIndexOfNextChildTokenOfEvent(PrinterEvent event) {
		if (event instanceof ElementPrinterEvent) {
			// in case of collection search for exact item of the collection
			ElementPrinterEvent elementEvent = (ElementPrinterEvent) event;
			return findIndexOfNextChildTokenOfElement(elementEvent.getElement());
		}
		return super.findIndexOfNextChildTokenOfEvent(event);
	}

	@Override
	protected void printOriginSpacesUntilFragmentIndex(int index) {
		super.printOriginSpacesUntilFragmentIndex(getLastWhiteSpaceBefore(index), index);
	}

	/**
	 * @return index of last child fragment which contains space, which is before `index`
	 */
	private int getLastWhiteSpaceBefore(int index) {
		for (int i = index - 1; i >= 0; i--) {
			SourceFragment fragment = childFragments.get(i);
			if (isSpaceFragment(fragment)) {
				continue;
			}
			return i + 1;
		}
		return 0;
	}

//	@Override
//	protected void setChildFragmentIdx(int idx) {
//		//never move current index, so we search always since beginning
//		//the order of items is defined by DJPP and not by sequence of items in origin sources
//	}
}
