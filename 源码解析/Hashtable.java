package JDK;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by 11981 on 2017/10/22.
 */
public class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, Serializable{
    /**
     * 哈希表中存放数据的地方
     */
    private transient Entry<?,?>[] table;
    /**
     * 哈希表中所有Entry的数目
     */

    private transient int count;
    /**
     * 重新哈希的阈值（threshold = (int)capacity * loadFactor）
     */

    private int threshold;
    //装载因子

    private float loadFactor;
    /**
     * Hashtable的结构修改次数。结构修改指的是改变Entry数目或是修改内部结构（如rehash）
     * 这个字段用来使Hashtable的集合视图fail-fast的。
     */

    private int modCount = 0;

    private static final long serialVersionUID = 1421746759512286392L;

    /**
     * 构造函数一：构造一个指定容量和装载因子的空哈希表
     * @param initialCapacity
     * @param loadFactor
     */
    public Hashtable(int initialCapacity, float loadFactor){
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal capacity:" + initialCapacity);
        if (loadFactor < 0 || Float.isNaN(loadFactor)){
            throw new IllegalArgumentException("Illegal load:" + loadFactor);
        }
        if (initialCapacity == 0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry<?,?>[initialCapacity];
        threshold = (int)Math.min(initialCapacity*loadFactor, MAX_ARRAY_SIZE);
    }

    /**
     * 构造函数二：构造一个指定容量的空哈希表
     * @param initialCapacity
     */

    public Hashtable(int initialCapacity){
        this(initialCapacity, 0.75f);
    }

    /**
     * 构造函数三：默认构造函数，容量为11，装载因子为0.75
     */

    public Hashtable(){
        this(11 , 0.75f);
    }

    /**
     * 构造函数四：构造一个包含子Map的构造函数，
     * 容量为足够容纳指定Map中元素的2的次幂，默认装载因子0.75
     */
    public Hashtable(Map<? extends K, ? extends V> map){
        this(Math.max(2*map.size(), 11), 0.75f);
        putAll(map);
    }
    public synchronized int size(){
        return count;
    }

    public synchronized boolean isEmpty(){
        return count == 0;
    }

    /**
     * 返回Hashtable中所有关键字的枚举集合，方法由synchronize修饰，支持同步调用
     * @return
     */
    public synchronized Enumeration<K> keys(){
        return this.<K>getEnumeration(KEYS);
    }

    public synchronized Enumeration<V> elements(){
        return this.<V>getEnumeration(VALUES);
    }

    /**
     * 测试Hashtable中是否有关键字映射到指定值上。contains(value)比containsKey(key)方法耗时多一些。
     * @param value
     * @return
     */
    public synchronized boolean contains(Object value){
        //Hashtable中的键值对的value不能为null，否则抛出异常NullPointerException
        if (value == null)
            throw new NullPointerException();
        Entry<?,?> tab[] = table;
        //从后往前遍历table数组中的元素(Entry)
        for (int i=tab.length; i-- > 0; ){
            //对于每个Entry（单向链表），逐个遍历，判断结点的值是否等于value
            for (Entry<?,?> e = tab[i]; e != null; e = e.next){
                if (e.value.equals(value)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsValue(Object value){
        return contains(value);
    }

    /**
     * 测试指定key是否存在
     * @param key
     * @return
     */
    public synchronized boolean containsKey(Object key){
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        //关键字key映射的bucket下标
        int index = (hash & 0x7FFFFFFF) % tab.length;
        //遍历单链表找到和key相等的元素
        for (Entry<?,?> e = tab[index]; e != null; e = e.next){
            if ((e.hash == hash) && e.key.equals(key)){
                return true;
            }
        }
        return false;
    }

    /**
     * 返回指定关键字key的value值，不存在则返回null
     * @param key
     * @return
     */
    public synchronized V get(Object key){
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        //计算指定关键字映射的哈希槽
        int index = (hash&0x7FFFFFFF) % tab.length;
        //遍历单向链表
        for (Entry<?,?> e = tab[index]; e != null; e = e.next){
            if ((e.hash == hash) && e.key.equals(key)){
                return (V)e.value;
            }
        }
        return null;
    }
    /**
     * 分配数组最大容量，一些虚拟机会保存数组的头字，试图分配更大的数组会导致OOM（OutOfMemoryError）：请求的数组容量超出VM限制
     */

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 容量增长时需要内部重新组织Hashtable，以便有效率的访问
     * 当Hashtable中关键字数据超出容量与装载因子之积时自动调用该方法
     *
     */
    protected void rehash(){
        int oldCapacity = table.length;//旧的容量
        Entry<?,?>[] oldMap = table;//旧Entry数组

        //新容量等于旧容量乘以2加1
        int newCapacity = (oldCapacity << 1) + 1;
        //溢出检测
        if (newCapacity - MAX_ARRAY_SIZE > 0){
            if (oldCapacity == MAX_ARRAY_SIZE)
                return;
            newCapacity = MAX_ARRAY_SIZE;
        }
        //申请新的Entry数组
        Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];
        //修改modCount
        modCount++;
        //修改新阈值
        threshold = (int)Math.min(newCapacity*loadFactor, MAX_ARRAY_SIZE+1);
        //把新的数组指向table
        table = newMap;
        //从后向前遍历旧表每一个槽中的链表的每一个Entry元素，将其重新哈希到新表中
        for (int i=oldCapacity; i-->0; ){
            for (Entry<K,V> old = (Entry<K, V>) oldMap[i]; old != null; ){
                Entry<K,V> e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                //将e插入index槽中当前链表的开头
                e.next = (Entry<K, V>) newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * 添加新的Entry元素
     * @param hash
     * @param key
     * @param value
     * @param index
     */
    private void addEntry(int hash, K key, V value, int index){
        modCount++;
        Entry<?,?> tab[] = table;
        //超过阈值，需要重新哈希
        if (count > threshold){
            rehash();
            tab = table;
            hash = key.hashCode();
            index = (hash & 0x7FFFFFFF)%tab.length;
        }

        //创建新的entry，并插入index槽的中链表的头部
        Entry<K,V> e = (Entry<K, V>) tab[index];
        tab[index] = new Entry<>(hash, key, value, e);
        count++;
    }

    private <T> Enumeration<T> getEnumeration(int type){
        if (count == 0){
            return Collections.emptyEnumeration();
        }else{
            return new Enumerator<>(type, true);
        }
    }

    private <T>Iterator<T> getIterator(int type){
        if (count == 0){
            return Collections.emptyIterator();
        }else{
            return new Enumerator<>(type, true);
        }
    }
    //将指定的key映射到指定的value。key和value都不能为null
    public synchronized V put(K key, V value){
        if (value == null)
            throw new NullPointerException();
        //确保key在Hashtable中不存在，若存在则更新value，返回旧值
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> entry = (Entry<K, V>) tab[index];
        for (; entry != null; entry = entry.next){
            if ((entry.hash == hash) && entry.key.equals(key)){
                V old = entry.value;
                entry.value = value;
                return old;
            }
        }
        //不存在，则添加元素
        addEntry(hash, key, value, index);
        return null;
    }

    /**
     * 删除关键字
     * @param key
     * @return
     */

    public synchronized V remove(Object key){
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash&0x7FFFFFFF)%tab.length;
        Entry<K, V> e = (Entry<K, V>) tab[index];
        for (Entry<K, V> prev = null; e != null; prev = e, e = e.next){
            //删除e
            if ((e.hash == hash) && e.key.equals(key)){
                modCount++;
                if (prev != null)
                    prev.next = e.next;
                else
                    tab[index] = e.next;

                count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }
    /**
     * 将指定Map中的所有映射都拷贝到Hashtable中，已经存在的Key对应的value值会被更新
     * @param t mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public synchronized void putAll(Map<? extends K, ? extends V> t){
        for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
            put(e.getKey(), e.getValue());
    }

    public synchronized void clear(){
        Entry<?, ?> tab[] = table;
        modCount++;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }

    public synchronized Object clone(){
        try {
            Hashtable<?,?> t = (Hashtable<?, ?>) super.clone();
            t.table = new Entry<?, ?>[table.length];
            for (int i=table.length; i-- > 0; ){
                t.table[i] = (table[i] != null) ? (Entry<?, ?>) table[i].clone() : null;
            }
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            t.modCount = 0;
            return t;
        }catch (CloneNotSupportedException e){
            throw new InternalError(e);
        }
    }

    public synchronized String toString(){
        int max = size() - 1;
        if (max == -1)
            return "{}";
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<K,V>> it = entrySet().iterator();

        sb.append("{");
        for (int i=0; ; i++){
            Map.Entry<K,V> e = it.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this map)" : key.toString());
            sb.append('=');
            sb.append(value == this ? "(this map)" : value.toString());
            if (i == max)
                return sb.append("}").toString();
            sb.append(", ");
        }
    }
    private transient volatile Set<K> keySet = null;
    private transient volatile Set<Map.Entry<K,V>> entrySet;
    private transient volatile Collection<V> values = null;

    public Set<K> keySet(){
        if (keySet == null)
            keySet = Collections.synchronizedSet(new KeySet());
        return keySet();
    }

    private class KeySet extends AbstractSet<K>{
        public Iterator<K> iterator(){
            return getIterator(KEYS);
        }
        public int size(){
            return count;
        }
        public boolean contains(Object o){
            return containsKey(o);
        }
        public boolean remove(Object o){
            return Hashtable.this.remove(o) != null;
        }
        public void clear(){
            Hashtable.this.clear();
        }
    }
    /**
     * 返回Map中映射的集合Set，Map中的改变会反映在Set中，反之亦是如此。
     * 迭代器遍历Set时，如果Map结构发生变化，迭代器行为未定义，除了通过迭代器自身的remove操作和setValue操作
     * 该Set支持通过Iterator.remove,Set.remove, removeAll, retainAll和clear操作删除元素
     * 不支持add和addAll操作
     */
    public Set<Map.Entry<K, V>> entrySet(){
        if (entrySet() == null)
            entrySet = Collections.synchronizedSet(new EntrySet());
        return entrySet;
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>>{
        //返回Entry的迭代器
        public Iterator<Map.Entry<K,V>> iterator(){
            return getIterator(ENTRIES);
        }

        public boolean add(Map.Entry<K,V> o){
            return super.add(o);
        }

        //是否包含该entry
        public boolean contains(Object o){
            //确定类型
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            Object key = entry.getKey();
            Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;

            for (Entry<?,?> e = tab[index]; e != null; e = e.next)
                if (e.hash == hash && e.equals(entry))
                    return true;
            return false;
        }

        public boolean remove(Object o){
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object key = entry.getKey();
            Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;

            Entry<K,V> e = (Entry<K,V>) tab[index];

            for (Entry<K,V> prev = null; e != null; prev = e, e = e.next){
                if (e.hash == hash && e.equals(entry)){
                    modCount++;
                    if (prev != null)
                        prev.next = e.next;
                    else
                        tab[index] = e.next;
                    count--;
                    e.value = null;
                    return true;
                }
            }
            return false;

        }
        public int size(){
            return count;
        }

        public void clear(){
            Hashtable.this.clear();
        }

    }

    //返回Map中所有值的集合视图，Map中的任何修改都会反映在集合中，反之亦是如此。如果集合遍历过程中，
    // Map发生了结构上的修改，迭代类型未定义

    public Collection<V> values(){
        if (values() == null)
            values = Collections.synchronizedCollection(new ValueCollection());
        return values;
    }

    private class ValueCollection extends AbstractCollection<V>{
        public Iterator<V> iterator(){
            return getIterator(VALUES);

        }

        public int size(){
            return count;
        }

        public boolean contains(Object o){
            return containsValue(o);
        }

        public void clear(){
            Hashtable.this.clear();
        }
    }

    //比较指定对象和当前Map判断是否相等
    public synchronized boolean equals(Object o){
        //同一个元素
        if (o == this){
            return true;
        }

        if (!(o instanceof Map))
            return false;
        Map<?, ?> t = (Map<?,?>)o;
        if (t.size() != size())
            return false;
        try {
            Iterator<Map.Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()){
                Map.Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(t.get(key) == null && t.containsKey(key)))
                        return false;

                }else{
                    if (!value.equals(t.get(key)))
                        return false;
                }
            }

        }catch (ClassCastException e){
            return false;
        }catch (NullPointerException e){
            return false;
        }
        return true;
    }

    /**
     * 返回Map的哈希值，Map中的每一个Entry的哈希值相加
     * @return
     */
    public synchronized int hashCode(){
        int h = 0;
        if (count == 0 || loadFactor > 0){
            return h;
        }
        loadFactor = -loadFactor;// mark hashCode computation in progress
        Entry<?,?>[] tab = table;
        for (Entry<?, ?> entry : tab){
            while (entry != null){
                h += entry.hashCode();
                entry = entry.next;
            }
        }
        loadFactor = - loadFactor;// mark hashCode computation completed
        return h;
    }

    public synchronized V getOrDefault(Object key, V defaultValue){
        V result = get(key);
        return (null ==result) ? defaultValue : result;
    }

    public synchronized void forEach(BiConsumer<? super K, ? super V> action){
        Objects.requireNonNull(action);//explicit check required in case table is empty;

        final int expectedModCount = modCount;

        Entry<?, ?>[] tab = table;
        for (Entry<?, ?> entry : tab){
            while (entry != null){
                action.accept((K)entry.key, (V)entry.value);
                entry = entry.next;

                if (expectedModCount != modCount){
                    throw new ConcurrentModificationException();
                }
            }
        }

    }

    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function){
        Objects.requireNonNull(function);//explicit check required in case table is empty;
        final int expectedModCount = modCount;

        Entry<K,V>[] tab = (Entry<K, V>[]) table;
        for (Entry<K,V> entry : tab){
            while (entry != null){
                entry.value = Objects.requireNonNull(function.apply(entry.key, entry.value));
                entry = entry.next;
                if (expectedModCount != modCount)
                    throw new ConcurrentModificationException();
            }
        }

    }


    //存在key就更新，不存在就添加
    public synchronized V putIfAbsent(K key, V value){
        Objects.requireNonNull(value);
        Entry<?, ?>tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> entry = (Entry<K, V>) tab[index];
        //如果存在
        for (; entry != null; entry = entry.next){
            if ((entry.hash == hash) && entry.key.equals(key)){
                V oldValue = entry.value;
                if (oldValue == null)
                    entry.value = value;
                return oldValue;
            }
        }
        //如果不存在，就添加新的entry
        addEntry(hash, key, value, index);
        return null;
    }
    //删除指定的键值对
    public synchronized boolean remove(Object key, Object value){
        Objects.requireNonNull(value);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> e = (Entry<K, V>) tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next){
            if ((e.hash == hash) && e.key.equals(key) && e.value.equals(value)){
                modCount ++;
                if (prev == null){
                    tab[index] = e.next;
                }else{
                    prev.next = e.next;
                }
                count--;
                e.value = null;
                return true;
            }
        }
        return false;
    }

    /**
     * 替换旧值为新值，如果key对应的值不等于旧值，旧不替换
     * @param key
     * @param oldValue
     * @param newValue
     * @return
     */
    public synchronized boolean replace(K key, V oldValue, V newValue){
        Objects.requireNonNull(oldValue);
        Objects.requireNonNull(newValue);
        Entry<?, ?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> e = (Entry<K, V>) tab[index];
        for (; e != null; e = e.next){
            if ((e.hash == hash) && e.key.equals(key)){
                if (e.value.equals(oldValue)){
                    e.value = newValue;
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    //替换旧值，如果不存在key，则返回null
    public synchronized V replace(K key, V value){
        Objects.requireNonNull(value);
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> e = (Entry<K, V>) tab[index];
        for (; e != null; e = e.next){
            if ((e.hash == hash) && e.key.equals(key)){
                V oldValue = e.value;
                e.value = value;
                return value;
            }
        }
        return null;
    }

    //如果不存在Key，就添加键值对key-value，value通过mappingFunction计算得到
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction){
        Objects.requireNonNull(mappingFunction);//如果为null，抛出异常
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> e = (Entry<K, V>) tab[index];
        for (; e != null; e = e.next){
            if ((e.hash == hash)&&e.key.equals(key)){
                return e.value;
            }
        }
        V newValue = mappingFunction.apply(key);
        if (newValue != null){
            addEntry(hash, key, newValue, index);
        }
        return newValue;
    }
    //如果存在就替换Key的value值，value通过mappingFunction计算得到，如果计算得到的value为null，就删除Key对应的Entry
    public synchronized V computeIfPresent(K key, BiFunction<? super K ,? super V, ? extends V> remappingFunction){
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next){
            if (e.hash == hash && e.key.equals(key)){
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null){
                    modCount++;
                    if (prev != null){
                        prev.next = e.next;
                    }else{
                        tab[index] = e.next;
                    }
                    count--;
                }else{
                    e.value = newValue;
                }
                return newValue;
            }

        }
        return null;
    }

    public synchronized V compute(K key, BiFunction<? super K ,? super V, ? extends V> remappingFunction){
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next){
            if (e.hash == hash && Objects.equals(e.key, key)){
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null){
                    modCount++;
                    if (prev == null){
                        tab[index] = e.next;
                    }else{
                        prev.next = e.next;
                    }
                    count--;
                }else{
                    e.value = newValue;
                }
                return newValue;
            }

        }
        V newValue = remappingFunction.apply(key, null);
        if (newValue != null){
            addEntry(hash, key, newValue, index);
        }
        return newValue;
    }

    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction){
        Objects.requireNonNull(remappingFunction);
        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index];
        for (Entry<K,V> prev = null; e != null; prev=e, e = e.next){
            if (e.hash == hash && e.key.equals(key)){
                V newValue = remappingFunction.apply(e.value, value);
                if (newValue == null){
                    modCount++;
                    if (prev == null){
                        tab[index] = e.next;
                    }else{
                        prev.next = e.next;
                    }
                    count--;
                }else{
                    e.value = newValue;
                }
                return newValue;
            }
        }
        if (value != null){
            addEntry(hash, key, value, index);
        }
        return value;

    }

    /**
     * 将Hashtable的状态保存进流中
     * @param s
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException{
        Entry<Object, Object> entryStack = null;
        synchronized (this){
            s.defaultWriteObject();
            s.writeInt(table.length);
            s.writeInt(count);
            for (int index = 0; index < table.length; index++){
                Entry<?, ?> entry = table[index];
                while (entry != null){
                    entryStack = new Entry<>(0, entry.key, entry.value, entryStack);
                    entry = entry.next;
                }
            }
        }
    }

    //从流中读取Hashtable
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException{
        // Read in the length, threshold, and loadfactor
        s.defaultReadObject();
        // Read the original length of the array and number of elements
        int origlength = s.readInt();
        int elements = s.readInt();
        int length = (int)(elements * loadFactor) + (elements/20) + 3;
        if (length > elements && (length & 1) == 0){
            length--;
        }
        if (origlength > 0 && length > origlength){
            length = origlength;
        }
        table = new Entry<?,?>[length];
        threshold = (int)Math.min(length * loadFactor, MAX_ARRAY_SIZE+1);
        count = 0;

        for (; elements > 0; elements--){
            K key = (K)s.readObject();
            V value = (V)s.readObject();
            reconstitutionPut(table, key, value);
        }
    }

    /**
     * readObject使用的put方法（重建put），因为put方法支持重写，并且子类尚未初始化的时候不能调用put方法，所以就提供了reconstitutionPut
     * 它和常规put方法有几点不同，不检测rehash,因为初始元素数目已知。modCount不会自增，因为我们是在创建一个新的实例。
     * 不需要返回值
     */
    private void reconstitutionPut(Entry<?,?>[] tab, K key, V value) throws StreamCorruptedException{
        if (value == null)
            throw new StreamCorruptedException();
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry<?,?> e = tab[index]; e != null; e = e.next){
            //如果反序列化过程中出现key值重复，则抛出StreamCorruptedException异常
            if ((e.hash == hash) && e.key.equals(key)){
                throw new StreamCorruptedException();
            }
        }

        //创建新的Entry
        Entry<K, V> e = (Entry<K, V>) tab[index];
        tab[index] = new Entry<>(hash, key, value, e);
        count++;
    }


    /**
     * Hashtable使用单向链表Entry解决哈希冲突
     */
    private static class Entry<K,V> implements Map.Entry<K,V>{
        final int hash;//哈希值，不可变
        final K key;//关键字，不可变
        V value;//值，可变
        Entry<K,V> next;//链表中指向下一个的节点

        protected Entry(int hash, K key, V value, Entry<K,V> next){
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        //返回一个自身的复制对象，浅拷贝，没有新建key和value对象
        protected Object clone(){
            return new Entry<>(hash, key, value, (next == null ? null : (Entry<K,V>)next.clone()));
        }

        public K getKey(){
            return key;
        }

        public V getValue(){
            return value;
        }

        public V setValue(V value){
            //Hashtable不允许空值
            if (value == null)
                throw new NullPointerException();
            V oldValue = this.value;
            this.value = value;
            //返回原先的值
            return oldValue;
        }
        //重写equals方法
        public boolean equals(Object o){
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            //先进行为null的判断，key和value都相同返回true
            return (key == null ? e.getKey()== null : e.getKey().equals(key)) &&
                    (value == null ? e.getValue() == null : value.equals(e.getValue()));
        }

        public int hashCode(){
            //hash值只与关键字key有关，hashCode需要与值value的hashCode异或
            return hash ^ Objects.hashCode(value);
        }

        public String toString(){
            return key.toString() + "=" + value.toString();
        }
    }

    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    private class Enumerator<T> implements Enumeration<T>, Iterator<T>{
        Entry<?,?> table[] = Hashtable.this.table;//由Hashtable的table数组支持
        int index = table.length;//table数组的长度
        Entry<?,?> entry = null;//下一个返回的元素
        Entry<?,?> lastReturned = null;//上一次返回的元素
        int type;//类型：KEYS，VALUES,Entries

        //表明当前枚举是作为一个迭代器还是一个枚举类型（true表示迭代器）
        boolean iterator;

        /**
         * 迭代器认为Hashtable应该拥有的modCount值。
         * 如果期望的不一致，迭代器就检测到并发修改了
         */
        protected int exceptedModCount = modCount;

        Enumerator(int type, boolean iterator){
            this.type = type;
            this.iterator = iterator;
        }

        public boolean hasMoreElements(){
            Entry<?,?> e = entry;
            int i = index;
            Entry<?,?>[] t = table;

            while (e == null && i > 0){
                e = t[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        public T nextElement(){
            Entry<?, ?> et = entry;
            int i = index;
            Entry<?,?>[] t = table;
            //上一个返回元素为空，表明开始返回第一个元素
            while (et == null && i > 0){
                et = t[--i];
            }
            entry = et;
            index = i;
            if (et != null){
                //更新上一个返回元素为当前即将返回的元素
                Entry<?,?> e = lastReturned = entry;
                entry = e.next;
                //类型为keys则返回Key，为value则返回value，否则返回Entry
                return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
            }
            throw new NoSuchElementException("Hashtable Enumeration");
        }

        public boolean hasNext(){
            return hasMoreElements();
        }

        public T next(){
            //检测并发修改异常
            if (modCount != exceptedModCount)
                throw new ConcurrentModificationException();
            return nextElement();
        }

        public void remove(){
            if (!iterator)
                throw new UnsupportedOperationException();
            if (lastReturned == null)
                throw new IllegalStateException("Hashtable Enumeration");
            if (modCount != exceptedModCount)
                throw new ConcurrentModificationException();
            synchronized (Hashtable.this){
                Entry<?,?>[] tab = Hashtable.this.table;
                int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

                Entry<K,V> e = (Entry<K, V>) tab[index];
                for (Entry<K,V> prev = null; e != null; prev = e, e = e.next){
                    if (e == lastReturned){
                        modCount++;
                        exceptedModCount++;
                        if (prev == null){
                            tab[index] = e.next;
                        }else{
                            prev.next = e.next;
                        }
                        count--;
                        lastReturned = null;
                        return;
                    }

                }
                throw new ConcurrentModificationException();
            }
        }
    }
}
