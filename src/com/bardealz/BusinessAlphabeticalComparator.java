package com.bardealz;

import java.util.Comparator;

public class BusinessAlphabeticalComparator implements Comparator<Business> {

	@Override
	public int compare(Business lhs, Business rhs) {
		return rhs.name.compareTo(lhs.name);
	}

}
