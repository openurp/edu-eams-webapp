/*
 * KINGSTAR MEDIA SOLUTIONS Co.,LTD. Copyright c 2005-2013. All rights reserved.
 *
 * This source code is the property of KINGSTAR MEDIA SOLUTIONS LTD. It is intended
 * only for the use of KINGSTAR MEDIA application development. Reengineering, reproduction
 * arose from modification of the original source, or other redistribution of this source
 * is not permitted without written permission of the KINGSTAR MEDIA SOLUTIONS LTD.
 */
package com.ekingstar.eams.teach.time.util;

import junit.framework.Assert;

import org.testng.annotations.Test;

@Test
public class TermCalculatorTest {

	public void testLess() {
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("3,5", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm(",3,5,", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("*", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("春", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("春,秋", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("春季", 4));
	}
	
	public void testLessOrEqual() {
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("3,5,4", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm(",3,5,4", 4));
		Assert.assertTrue(TermCalculator.lessOrEqualTerm("4", 4));
	}
}
