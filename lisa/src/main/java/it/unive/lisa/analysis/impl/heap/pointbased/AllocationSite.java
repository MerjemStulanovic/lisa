package it.unive.lisa.analysis.impl.heap.pointbased;

import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.HeapLocation;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.externalSet.ExternalSet;

/**
 * A heap identifier that track also the numerical identifier of the
 * corresponding heap location and a field (optional). This class is used in
 * {@link PointBasedHeap}.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 */
public class AllocationSite extends HeapLocation {

	private final String id;

	/**
	 * Builds the an allocation site from its numerical identifier.
	 * 
	 * @param types the runtime types of this allocation site
	 * @param id    the identifier of this allocation site
	 */
	public AllocationSite(ExternalSet<Type> types, String id) {
		super(types, "pp@" + id, false);
		this.id = id;
	}

	public AllocationSite(ExternalSet<Type> types, String id, boolean isWeak) {
		super(types, "pp@" + id, isWeak);
		this.id = id;
	}

	/**
	 * Builds the an allocation site from its numerical identifier and its
	 * field.
	 * 
	 * @param types the runtime types of this allocation site
	 * @param id    the identifier of this allocation site
	 * @param field the field of this allocation site
	 */
	public AllocationSite(ExternalSet<Type> types, String id, SymbolicExpression field) {
		super(types, "pp@" + id + "[" + field + "]", false);
		this.id = id;
	}

	/**
	 * Returns the numerical identifier of this allocation site.
	 * 
	 * @return the numerical identifier of this allocation site
	 */
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return getName() + "[" + (isWeak() ? "w" : "s") + "]";
	}
}
