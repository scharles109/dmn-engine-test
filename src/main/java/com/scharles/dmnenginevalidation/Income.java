package com.scharles.dmnenginevalidation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Income {
	
	private CreditRating creditRating;

	public enum CreditRating {
		EXCELLENT,
		GOOD,
		BAD,
		POOR;
	}
}
