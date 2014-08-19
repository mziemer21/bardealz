package com.bardealz;

import java.util.Comparator;

public class BusinessAlphabeticalComparator implements Comparator<Business> {

	@Override
	public int compare(Business lhs, Business rhs) {
		return lhs.name.compareTo(rhs.name);
	}

}
