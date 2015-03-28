package org.openurp.edu.eams.web.view.component




import org.apache.struts2.util.MakeIterator
import org.beangle.commons.collection.Collections
import org.beangle.commons.entity.HierarchyEntity
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack
import Menu._




object Menu {

  class Option(stack: ValueStack) extends HierarchyUIBean[Option](stack) {

    private var menu: Menu = findAncestor(classOf[Menu]).asInstanceOf[Menu]

    private var var_index: String = menu.`var` + "_all_index"

    private var allVar_index: String = _

    private var iterator: Iterator[_] = MakeIterator.convert(iteratorTarget)

    private var index: Int = -1

    protected var curObj: AnyRef = _

    val iteratorTarget = menu.items

    if (!iterator.hasNext) {
      iterator = Collections.singleton(null).iterator()
    }

    protected override def next(): Boolean = {
      var result = false
      if (curObj.isInstanceOf[HierarchyEntity]) {
        if (!curObj.asInstanceOf[HierarchyEntity].getChildren.isEmpty) {
          menu.curOption = this
          result = nextChild()
        }
      } else if (iterator != null && iterator.hasNext) {
        curObj = iterator.next()
        result = true
      } else {
        result = parentNext()
      }
      if (result) {
        index += 1
        menu.allIndex += 1
        stack.getContext.put(menu.`var`, curObj)
        stack.getContext.put(var_index, index)
        stack.getContext.put(allVar_index, menu.allIndex)
      } else {
        stack.getContext.remove(menu.`var`)
        stack.getContext.remove(var_index)
        stack.getContext.remove(allVar_index)
      }
      result
    }

    protected def nextChild(): Boolean = {
      val childIterator = curObj.asInstanceOf[HierarchyEntity].getChildren.iterator()
      if (childIterator.hasNext) {
        curObj = childIterator.next()
        return true
      }
      false
    }

    protected override def parentNext(): Boolean = {
      if (null != menu.curOption && null != menu.curOption.iterator && 
        menu.curOption.iterator.hasNext) {
        curObj = menu.curOption.iterator.next()
      }
      false
    }
  }
}

class Menu(stack: ValueStack) extends ClosingUIBean(stack) {

  
  var label: String = _

  
  var title: String = _

  
  var multi: String = _

  
  var name: String = _

  
  var onChange: String = _

  
  var onClick: String = _

  
  var initCallback: String = _

  
  var autocomplete: String = "false"

  
  var empty: AnyRef = _

  
  var required: AnyRef = _

  
  var items: List[_] = new ArrayList[Any]()

  private var allIndex: Int = -1

  private var curOption: Option = _

  
  var `var`: String = _

  
  var value: String = _

  
  var filters: Map[String, String] = Collections.newMap[Any]

  protected override def evaluateParams() {
    if (null == this.id) generateIdIfEmpty()
    if (null != label) label = getText(label, label)
    title = if (null != title) getText(title) else label
    required = "true" == required + ""
  }
}
