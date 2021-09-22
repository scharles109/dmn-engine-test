package com.scharles.dmnenginevalidation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import com.scharles.dmnenginevalidation.DMNEngineTest.DMNEngineConfiguration;
import com.scharles.dmnenginevalidation.Income.CreditRating;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.dmn.api.core.DMNResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DMNEngineConfiguration.class})
public class DMNEngineTest {
	
	@Configuration
	public static class DMNEngineConfiguration {

		@Bean
		public DMNEngine dmnEngine() {
			return new DMNEngine();
		}
	}

	@Autowired
	private DMNEngine dmnEngine;

	@Test
	public void shouldTestEnum() throws IllegalAccessException {
		Income income = new Income();
		income.setCreditRating(CreditRating.EXCELLENT);

		dmnEngine.initialize();

		Map<String, Income> incomeMap = new HashMap<>();
		incomeMap.put("income", income);

		DMNResult dmnResult = dmnEngine.execute(incomeMap);

		assertEquals(1, dmnResult.getDecisionResults().size());
	}
}
