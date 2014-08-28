package com.bardealz;

import java.util.Comparator;

public class BusinessRatingComparator implements Comparator<Business> {

	@Override
	public int compare(Business lhs, Business rhs) {
		return lhs.rating.compareTo(rhs.rating);
	}
}
