package me.gegenbauer.logviewer.databinding

class ObservableViewModelProperty<T>(value: T?): ObservableProperty<T>(value) {

    /**
     * notify component to update with default value of [ObservableViewModelProperty] when binding first created
     */
    override fun addObserver(observer: Observer<T>) {
        super.addObserver(observer)
        observer.onChange(value)
    }
}