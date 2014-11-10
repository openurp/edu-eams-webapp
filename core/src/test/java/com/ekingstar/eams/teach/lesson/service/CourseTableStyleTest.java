/*
 * KINGSTAR MEDIA SOLUTIONS Co.,LTD. Copyright c 2005-2013. All rights reserved.
 *
 * This source code is the property of KINGSTAR MEDIA SOLUTIONS LTD. It is intended
 * only for the use of KINGSTAR MEDIA application development. Reengineering, reproduction
 * arose from modification of the original source, or other redistribution of this source
 * is not permitted without written permission of the KINGSTAR MEDIA SOLUTIONS LTD.
 */
package com.ekingstar.eams.teach.lesson.service;

import static org.testng.Assert.assertEquals;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

@Test
public class CourseTableStyleTest {

	public void testValueOf() {
		CourseTableStyle style1 = CourseTableStyle.valueOf("WEEK_TABLE");
		AssertJUnit.assertNotNull(style1);
		AssertJUnit.assertEquals(style1, CourseTableStyle.WEEK_TABLE);

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNotExists() {
		CourseTableStyle.valueOf("WEEK_TABLE1");
	}

	public void testGet() {
		assertEquals(CourseTableStyle.getStyle("aaa"), CourseTableStyle.WEEK_TABLE);
		assertEquals(CourseTableStyle.getStyle("WEEK_TABLE"), CourseTableStyle.WEEK_TABLE);
		assertEquals(CourseTableStyle.getStyle("LIST"), CourseTableStyle.LIST);
		assertEquals(CourseTableStyle.getStyle("UNIT_COLUMN"), CourseTableStyle.UNIT_COLUMN);

		assertEquals(CourseTableStyle.getStyle(null), CourseTableStyle.WEEK_TABLE);
	}

}
