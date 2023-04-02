package me.gegenbauer.logviewer.databinding

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

interface Observable<T> {

    fun addObserver(observer: Observer<T>)

    fun removeObserver(observer: Observer<T>?)

}

fun interface Observer<T> {
    fun onChange(newValue: T?)
}

class ComponentPropertyObserver<T>(
    private val callback: (newValue: T?) -> Unit
) : Observer<T> {
    override fun onChange(newValue: T?) {
        callback(newValue)
    }
}

class ViewModelPropertyObserver<T>(
    private val callback: (newValue: T?) -> Unit
) : Observer<T> {
    override fun onChange(newValue: T?) {
        callback(newValue)
    }
}


open class ObservableProperty<T>(var value: T? = null) : Observable<T> {
    private val lock = ReentrantReadWriteLock()

    private val obs = mutableListOf<Observer<T>>()

    open fun updateValue(newValue: T?) {
        if (this.value == newValue) return
        this.value = newValue
        lock.read { obs.forEach { it.onChange(newValue) } }
    }

    // 获取泛型类型的类名称
    protected fun getValueType(): String {
        value ?: return "TYPE_NULL"
        return value!!::class.java.simpleName
    }

    open fun getDisplayName(): String {
        return getValueType()
    }

    override fun addObserver(observer: Observer<T>) {
        lock.write { obs.add(observer) }
    }

    override fun removeObserver(observer: Observer<T>?) {
        if (observer == null) {
            return
        }
        lock.write {
            obs.remove(observer)
        }
    }

}