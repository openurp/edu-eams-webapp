package org.openurp.edu.eams.util

import java.text.Collator
import java.util.Comparator
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import org.beangle.commons.lang.Strings




class PinyinComparator extends Comparator[String]() {

  
  var asc: Boolean = _

  
  var collator: Collator = Collator.getInstance

  
  var pinyinOutputFormat: HanyuPinyinOutputFormat = new HanyuPinyinOutputFormat()

  pinyinOutputFormat.caseType=HanyuPinyinCaseType.LOWERCASE

  pinyinOutputFormat.toneType=HanyuPinyinToneType.WITHOUT_TONE

  def this(asc: Boolean) {
    this()
    this.asc = asc
  }

  def this(asc: Boolean, collator: Collator) {
    this()
    this.collator = collator
    this.asc = asc
  }

  def compare(s1: String, s2: String): Int = {
    if (null == s1) {
      s1 = ""
    }
    if (null == s2) {
      s2 = ""
    }
    if (s1 == s2) {
      return 0
    } else if (Strings.isEmpty(s1) || Strings.isEmpty(s2)) {
      return (if (asc) 1 else -1) * (collator.compare(s1, s2))
    } else {
      val c1 = s1.toCharArray()
      val c2 = s2.toCharArray()
      val sb1 = new StringBuilder()
      val sb2 = new StringBuilder()
      try {
        for (i <- 0 until c1.length) {
          val pinyin1 = PinyinHelper.toHanyuPinyinStringArray(c1(i), pinyinOutputFormat)
          if (null != pinyin1) {
            for (str <- pinyin1) {
              sb1.append(str)
            }
          }
        }
        for (i <- 0 until c2.length) {
          val pinyin2 = PinyinHelper.toHanyuPinyinStringArray(c2(i), pinyinOutputFormat)
          if (null != pinyin2) {
            for (str <- pinyin2) {
              sb2.append(str)
            }
          }
        }
        return (if (asc) 1 else -1) * (collator.compare(sb1.toString, sb2.toString))
      } catch {
        case e: BadHanyuPinyinOutputFormatCombination => e.printStackTrace()
      }
    }
    0
  }
}
