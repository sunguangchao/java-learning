
import java.util.*;

/**
 * Created by 11981 on 2017/9/10.
 */
public class Vector<E> extends AbstractList<E>
        implements List<E>,RandomAccess,Cloneable,java.io.Serializable{
    // 保存Vector中数据的数组
    protected Object[] elementData;
    protected int elementCount;//实际数据的数量
    protected int capacityIncrement;//容量增长系数

    // Vector的序列版本号
    private static final long serialVersionUID = -2767605614048989439L;

    //默认容量为10
    public Vector(){
        this(10);
    }

    // 指定Vector容量大小的构造函数
    public Vector(int initialCapacity){
        this(initialCapacity, 0);
    }

    // 指定Vector"容量大小"和"增长系数"的构造函数
    public Vector(int initialCapacity, int capacityIncrement){
        super();
        if (capacityIncrement < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        // 新建一个数组，数组容量是initialCapacity
        this.elementData = new Object[initialCapacity];
        // 设置容量增长系数
        this.capacityIncrement = capacityIncrement;
    }

    public Vector(Collection<? extends E> c){
        // 获取“集合(c)”的数组，并将其赋值给elementData
        elementData = c.toArray();
        elementCount = elementData.length;

        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, elementCount, Object[].class);
    }

    // 将数组Vector的全部元素都拷贝到数组anArray中
    public synchronized void copyInto(Object[] anArray){
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    // 将当前容量值设为 =实际元素个数
    public synchronized void tirmToSize(){
        modCount++;
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity){
            //Arrays.copyOf ?
            elementData = Arrays.copyOf(elementData, elementCount);
        }
    }

    // 确认“Vector容量”的帮助函数
    private void ensureCapacityHelper(int minCapacity){
        int oldCapacity = elementData.length;
        // 当Vector的容量不足以容纳当前的全部元素，增加容量大小。
        // 若 容量增量系数>0(即capacityIncrement>0)，则将容量增大capacityIncrement
        // 否则，将容量增大一倍
        if (minCapacity > oldCapacity){
            Object[] oldData = elementData;
            int newCapacity = (capacityIncrement > 0) ?
                    (oldCapacity + capacityIncrement) : (oldCapacity * 2);
            if (newCapacity < minCapacity){
                newCapacity = minCapacity;
            }
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    // 确定Vector的容量
    public synchronized void ensureCapacity(int minCapacity){
        // 将Vector的改变统计数+1
        modCount++;
        ensureCapacityHelper(minCapacity);
    }
    //设置容量值为 newSize
    public synchronized void setSize(int newSize){
        modCount++;
        if (newSize > elementCount){
            // 若 "newSize 大于 Vector容量"，则调整Vector的大小。
            ensureCapacityHelper(newSize);
        }else{
            // 若 "newSize 小于/等于 Vector容量"，则将newSize位置开始的元素都设置为null
            for (int i = newSize; i < elementCount; i++){
                elementData[i] = null;
            }
        }
        elementCount = newSize;
    }

    // 返回“Vector的总的容量”
    public synchronized int capacity(){
        return elementData.length;
    }
    // 返回“Vector的实际大小”，即Vector中元素个数
    public synchronized int size(){
        return elementCount;
    }

    // 判断Vector是否为空
    public synchronized boolean isEmpty(){
        return elementCount == 0;
    }

    // 返回“Vector中全部元素对应的Enumeration”
    public Enumeration<E> elements(){

        // 通过匿名类实现Enumeration
        return new Enumeration<E>() {
            int count = 0;
            @Override
            public boolean hasMoreElements() {
                return count < elementCount;
            }

            @Override
            public E nextElement() {
                synchronized (Vector.this){
                    if (count < elementCount){
                        return (E)elementData[count++];
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    // 返回Vector中是否包含对象(o)
    public boolean contains(Object o){
        return indexOf(o, 0) >= 0;
    }

    // 从index位置开始向后查找元素(o)。
    // 若找到，则返回元素的索引值；否则，返回-1
    public synchronized int indexOf(Object o, int index){
        if (o == null){
            for (int i = index; i < elementCount; i++)
                if (elementData[i] == null)
                    return i;
        }else{
            for (int i = index; i < elementCount; i++)
                if (o.equals(elementData[i]))
                    return i;

        }
        return -1;
    }

    // 查找并返回元素(o)在Vector中的索引值
    public int indexOf(Object o){
        return indexOf(o, 0);
    }

    // 从后向前查找元素(o)。并返回元素的索引
    public synchronized int lastIndexOf(Object o){
        return lastIndexOf(o, elementCount-1);
    }

    //从后向前查找元素(o)。开始位置是从前向后的第index个数；
    //若找到，则返回元素的“索引值”；否则，返回-1。
    public synchronized int lastIndexOf(Object o, int index){
        if (index >= elementCount)
            throw new IndexOutOfBoundsException(index + " >= " + elementCount);
        if (o == null){
            // 若查找元素为null，则反向找出null元素，并返回它对应的序号

            for (int i = index; i >= 0; i--)
                if (elementData[i] == null)
                    return i;
        }else{
            // 若查找元素不为null，则反向找出该元素，并返回它对应的序号
            for (int i = index; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    // 返回Vector中index位置的元素。
    // 若index越界，则抛出异常
    public synchronized E elementAt(int index){
        if (index >= elementCount){
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }
        return (E)elementData[index];
    }
    //获取Vector的第一个元素，若失败，抛出异常
    public synchronized E firstElement(){
        if (elementCount == 0){
            throw new NoSuchElementException();
        }
        return (E)elementData[0];
    }

    //获取Vector的最後一个元素，若失败，抛出异常
    public synchronized E lastElement(){
        if (elementCount == 0){
            throw new NoSuchElementException();
        }
        return (E)elementData[elementCount-1];
    }

    //设置index位置的元素值为obj
    public synchronized void setElementAt(E obj, int index){
        if (index > elementCount){
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }
        elementData[index] = obj;
    }

    //删除位置index的元素
    public synchronized void removeElementAt(int index){
        modCount++;
        if (index > elementCount){
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }else if(index < 0){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0){
            System.arraycopy(elementData, index+1, elementData, index, j);

        }
        elementCount--;
        elementData[elementCount] = null;/* to let gc do its work */
    }

    // 在index位置处插入元素(obj)
    public synchronized void insertElementAt(E obj, int index){
        modCount++;
        if (index > elementCount){
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);

        }
        ensureCapacityHelper(elementCount + 1);
        System.arraycopy(elementData, index, elementData, index+1, elementCount-index);
    }


    // 将“元素obj”添加到Vector末尾
    public synchronized void addElement(E obj){
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;

    }

    // 在Vector中查找并删除元素obj。
    public synchronized boolean removeElement(Object obj){
        modCount++;
        int i = indexOf(obj);
        if (i >= 0){
            removeElementAt(i);
            return true;
        }
        return false;
    }
    // 删除Vector中的全部元素
    public synchronized void removeAllElements(){
        modCount++;
        for (int i=0; i < elementCount; i++)
            elementData[i] = null;
        elementCount = 0;
    }


    // 克隆函数
    public synchronized Object clone(){
        try {
            Vector<E> v = (Vector<E>) super.clone();
            v.elementData = Arrays.copyOf(elementData, elementCount);
            v.modCount = 0;
            return v;
        }catch (CloneNotSupportedException e){
            throw new InternalError();
        }
    }

    // 返回Object数组
    public synchronized Object[] toArray(){
        return Arrays.copyOf(elementData, elementCount);
    }

    // 返回Vector的模板数组。所谓模板数组，即可以将T设为任意的数据类型
    public synchronized <T> T[] toArray(T[] a){
        // 若数组a的大小 < Vector的元素个数；
        // 则新建一个T[]数组，数组大小是“Vector的元素个数”，并将“Vector”全部拷贝到新数组中
        if (a.length < elementCount)
            return (T[]) Arrays.copyOf(elementData, elementCount, a.getClass());
        // 若数组a的大小 >= Vector的元素个数
        // 则将Vector的全部元素都拷贝到数组a中。
        System.arraycopy(elementData, 0, a, 0, elementCount);
        if (a.length > elementCount)
            a[elementCount] = null;
        return a;
    }

    // 设置index位置的值为element。并返回index位置的原始值
    public synchronized E set(int index, E element){
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);
        Object oldValue = elementData[index];
        elementData[index] = element;
        return (E)oldValue;
    }


    // 将“元素e”添加到Vector最后。
    public synchronized boolean add(E e){
        modCount++;
        ensureCapacityHelper(elementCount+1);
        elementData[elementCount++] = e;
        return true;
    }

    // 删除Vector中的元素o
    public boolean remove(Object o){
        return removeElement(o);
    }

    // 在index位置添加元素element
    public void add(int index, E element){
        insertElementAt(element, index);
    }

    public synchronized E remove(int index){
        modCount++;
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);
        Object oldValue = elementData[index];
        int numMoved = elementCount - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index, numMoved);
        elementData[--elementCount] = null;
        return (E)oldValue;
    }

    // 清空Vector
    public void clear(){
        removeAllElements();
    }

    // 返回Vector是否包含集合c
    public synchronized boolean containsAll(Collection<?> c){
        return super.containsAll(c);
    }

    // 将集合c添加到Vector中
    public synchronized boolean addAll(Collection<? extends E> c){
        modCount++;
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount = numNew);
        System.arraycopy(a, 0, elementData, elementCount, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    // 删除集合c的全部元素
    public synchronized boolean removeAll(Collection<?> c){
        return super.removeAll(c);
    }

    // 删除“非集合c中的元素”
    public synchronized boolean retianAll(Collection<?> c){
        return super.retainAll(c);
    }

    // 从index位置开始，将集合c添加到Vector中
    public synchronized boolean addAll(int index, Collection<? extends E> c){
        modCount++;
        if (index < 0 || index > elementCount)
            throw new ArrayIndexOutOfBoundsException(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        int numMoved = elementCount - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index+numNew, numMoved);
        System.arraycopy(a, 0, elementData, index, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    public synchronized boolean equals(Object o){
        return super.equals(o);
    }

    public synchronized int hashCode(){
        return super.hashCode();
    }

    public synchronized String toString(){
        return super.toString();
    }

    // 获取Vector中fromIndex(包括)到toIndex(不包括)的子集
    public synchronized List<E> subList(int fromIndex, int toIndex){
        return Collections.synchronizedList(super.subList(fromIndex,toIndex), this);
    }

    protected synchronized void removeRange(int fromIndex, int toIndex){
        modCount++;
        int numMoved = elementCount - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
        int newElementCount = elementCount - (toIndex - fromIndex);
        while (elementCount != newElementCount)
            elementData[--elementCount] = null;
    }

    private synchronized void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException{
        s.defaultWriteObject();
    }

}
