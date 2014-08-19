package com.bardealz;

import java.util.Comparator;

public class BusinessRatingComparator implements Comparator<Business> {

	@Override
	public int compare(Business arg0, Business arg1) {
		return arg1.rating.compareTo(arg0.rating);
	}
}
