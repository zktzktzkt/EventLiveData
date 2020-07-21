package github.leavesc.eventlivedata

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * 作者：leavesC
 * 时间：2020/7/9 22:37
 * 描述：
 * GitHub：https://github.com/leavesC
 */
class EventLiveData<T> {

    private companion object {

        private const val START_VERSION = -1

        private val NOT_SET = Any()

        private fun assertMainThread(methodName: String) {
            check(EventTaskExecutor.isMainThread) {
                ("Cannot invoke $methodName on a background thread")
            }
        }

    }

    /* synthetic access */
    private val mDataLock = Any()

    private val mObservers =
        EventSafeIterableMap<Observer<T>, ObserverWrapper<T>>()

    // how many observers are in active state
    internal var mActiveCount = 0

    @Volatile
    private var mData: Any?

    // when setData is called, we set the pending data and actual data swap happens on the main
    // thread
    /* synthetic access */
    @Volatile
    private var mPendingData: Any? =
        NOT_SET

    private var mVersion: Int

    private var mDispatchingValue = false

    private var mDispatchInvalidated = false

    private val mPostValueRunnable = Runnable {
        var newValue: Any?
        synchronized(mDataLock) {
            newValue = mPendingData
            mPendingData =
                NOT_SET
        }
        setValue(newValue as T)
    }

    val value: T?
        get() = getValueInner()

    /**
     * Creates a LiveData initialized with the given `value`.
     *
     * @param value initial value
     */
    constructor(value: T) {
        mData = value
        mVersion = START_VERSION + 1
    }

    /**
     * Creates a LiveData with no value assigned to it.
     */
    constructor() {
        mData =
            NOT_SET
        mVersion =
            START_VERSION
    }

    private fun considerNotify(observer: ObserverWrapper<T>) {
        if (!observer.mActive) {
            return
        }
        // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
        //
        // we still first check observer.active to keep it as the entrance for events. So even if
        // the observer moved to an active state, if we've not received that event, we better not
        // notify for a more predictable notification order.
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        if (observer.mLastVersion >= mVersion) {
            return
        }
        observer.mLastVersion = mVersion
        observer.mObserver.onChanged(mData as T)
    }

    /* synthetic access */
    internal fun dispatchingValue(observerWrapper: ObserverWrapper<T>?) {
        var initiator: ObserverWrapper<T>? = observerWrapper
        if (mDispatchingValue) {
            mDispatchInvalidated = true
            return
        }
        mDispatchingValue = true
        do {
            mDispatchInvalidated = false
            if (initiator != null) {
                considerNotify(initiator)
                initiator = null
            } else {
                val iterator: Iterator<Map.Entry<Observer<T>, ObserverWrapper<T>>> =
                    mObservers.iteratorWithAdditions()
                while (iterator.hasNext()) {
                    considerNotify(iterator.next().value)
                    if (mDispatchInvalidated) {
                        break
                    }
                }
            }
        } while (mDispatchInvalidated)
        mDispatchingValue = false
    }

    /**
     * 在生命周期安全的整体保障上和 LiveData 完全一样
     * 在 onResume 之后和 onDestroy 之前均能收到 Observer 回调
     * @param owner
     * @param observer
     */
    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        observeLifecycleObserver(
            funName = "observe",
            owner = owner,
            observer = observer,
            isEvent = false,
            isLifecycleIntensive = true
        )
    }

    /**
     * 在生命周期安全的整体保障上和 LiveData 完全一样
     * 在 onResume 时接收 Observer 回调，并在 onDestroy 时自动移除监听
     * 但此方法不会向 Observer 回调旧值，即 EventLiveData 只会向 Observer 回调在调用 observeEvent 之后收到的值
     * @param owner
     * @param observer
     */
    @MainThread
    fun observeEvent(owner: LifecycleOwner, observer: Observer<T>) {
        observeLifecycleObserver(
            funName = "observeEvent",
            owner = owner,
            observer = observer,
            isEvent = true,
            isLifecycleIntensive = true
        )
    }

    /**
     * 相比 LiveData 会具备更长的生命周期
     * 在 onCreate 之后和 onDestroy 之前均能收到 Observer 回调，并在 onDestroy 时自动移除监听
     * @param owner
     * @param observer
     */
    @MainThread
    fun observeAlive(owner: LifecycleOwner, observer: Observer<T>) {
        observeLifecycleObserver(
            funName = "observeAlive",
            owner = owner,
            observer = observer,
            isEvent = false,
            isLifecycleIntensive = false
        )
    }

    /**
     * 相比 LiveData 会具备更长的生命周期
     * 在 onResume 时接收 Observer 回调，并在 onDestroy 前自动移除监听
     * 但此方法不会向 Observer 回调旧值，即 EventLiveData 只会向 Observer 回调在调用 observeEvent 之后收到的值
     * @param owner
     * @param observer
     */
    @MainThread
    fun observeAliveEvent(owner: LifecycleOwner, observer: Observer<T>) {
        observeLifecycleObserver(
            funName = "observeAliveEvent",
            owner = owner,
            observer = observer,
            isEvent = true,
            isLifecycleIntensive = false
        )
    }

    /**
     * 不具备生命周期安全的保障，使用上和 LiveData 完全一样
     * @param observer
     */
    @MainThread
    fun observeForever(observer: Observer<T>) {
        observeAlwaysActiveObserver(
            funName = "observeForever",
            observer = observer,
            isEvent = false
        )
    }

    /**
     * 不具备生命周期安全的保障
     * 此方法不会向 Observer 回调旧值，即 EventLiveData 只会向 Observer 回调在调用 observeEvent 之后收到的值
     * @param observer
     */
    @MainThread
    fun observeEventForever(observer: Observer<T>) {
        observeAlwaysActiveObserver(
            funName = "observeEventForever",
            observer = observer,
            isEvent = true
        )
    }

    private fun observeLifecycleObserver(
        funName: String,
        owner: LifecycleOwner,
        observer: Observer<T>,
        isEvent: Boolean,
        isLifecycleIntensive: Boolean
    ) {
        assertMainThread(
            funName
        )
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val lastVersion = if (isEvent) mVersion else START_VERSION
        val stateAtLeast =
            if (isLifecycleIntensive) Lifecycle.State.STARTED else Lifecycle.State.CREATED
        val wrapper = LifecycleBoundObserver(
            this,
            observer,
            lastVersion,
            owner,
            stateAtLeast
        )
        val existing = mObservers.putIfAbsent(observer, wrapper)
        require(!(existing != null && !existing.isAttachedTo(owner))) {
            ("Cannot add the same observer with different lifecycles")
        }
        if (existing != null) {
            return
        }
        owner.lifecycle.addObserver(wrapper)
    }


    private fun observeAlwaysActiveObserver(
        funName: String,
        observer: Observer<T>,
        isEvent: Boolean
    ) {
        assertMainThread(
            funName
        )
        val wrapper = AlwaysActiveObserver(
            this,
            observer,
            if (isEvent) mVersion else START_VERSION
        )
        val existing = mObservers.putIfAbsent(observer, wrapper)
        require(existing !is LifecycleBoundObserver) {
            ("Cannot add the same observer with different lifecycles")
        }
        if (existing != null) {
            return
        }
        wrapper.activeStateChanged(true)
    }


    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
    @MainThread
    fun removeObserver(observer: Observer<T>) {
        assertMainThread(
            "removeObserver"
        )
        val removed = mObservers.remove(observer) ?: return
        removed.detachObserver()
        removed.activeStateChanged(false)
    }

    /**
     * Removes all observers that are tied to the given [LifecycleOwner].
     *
     * @param owner The `LifecycleOwner` scope for the observers to be removed.
     */
    @MainThread
    fun removeObservers(owner: LifecycleOwner) {
        assertMainThread(
            "removeObservers"
        )
        for ((key, value) in mObservers) {
            if (value.isAttachedTo(owner)) {
                removeObserver(key)
            }
        }
    }

    fun postValue(value: T) {
        if (EventTaskExecutor.isMainThread) {
            setValue(value)
        } else {
            postValueReal(value)
        }
    }

    /**
     * Posts a task to a main thread to set the given value. So if you have a following code
     * executed in the main thread:
     * <pre class="prettyprint">
     * liveData.postValue("a");
     * liveData.setValue("b");
    </pre> *
     * The value "b" would be set at first and later the main thread would override it with
     * the value "a".
     *
     *
     * If you called this method multiple times before a main thread executed a posted task, only
     * the last value would be dispatched.
     *
     * @param value The new value
     */
    private fun postValueReal(value: T) {
        var postTask: Boolean
        synchronized(mDataLock) {
            postTask = mPendingData === NOT_SET
            mPendingData = value
        }
        if (!postTask) {
            return
        }
        EventTaskExecutor.postToMainThread(
            mPostValueRunnable
        )
    }

    /**
     * Sets the value. If there are active observers, the value will be dispatched to them.
     *
     *
     * This method must be called from the main thread. If you need set a value from a background
     * thread, you can use [.postValue]
     *
     * @param value The new value
     */
    @MainThread
    private fun setValue(value: T) {
        assertMainThread(
            "setValue"
        )
        mVersion++
        mData = value
        dispatchingValue(null)
    }

    /**
     * Returns the current value.
     * Note that calling this method on a background thread does not guarantee that the latest
     * value set will be received.
     *
     * @return the current value
     */
    private fun getValueInner(): T? {
        val data = mData
        return if (data !== NOT_SET) {
            data as T
        } else null
    }

    private fun getVersion(): Int {
        return mVersion
    }

    /**
     * Called when the number of active observers change to 1 from 0.
     *
     *
     * This callback can be used to know that this LiveData is being used thus should be kept
     * up to date.
     */
    open fun onActive() {

    }

    /**
     * Called when the number of active observers change from 1 to 0.
     *
     *
     * This does not mean that there are no observers left, there may still be observers but their
     * lifecycle states aren't [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED]
     * (like an Activity in the back stack).
     *
     *
     * You can check if there are observers via [.hasObservers].
     */
    open fun onInactive() {

    }

    /**
     * Returns true if this LiveData has observers.
     *
     * @return true if this LiveData has observers
     */
    fun hasObservers(): Boolean {
        return mObservers.size() > 0
    }

    /**
     * Returns true if this LiveData has active observers.
     *
     * @return true if this LiveData has active observers
     */
    fun hasActiveObservers(): Boolean {
        return mActiveCount > 0
    }

}