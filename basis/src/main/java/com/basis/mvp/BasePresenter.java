package com.basis.mvp;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * @author gyn
 * @date 2021/9/24
 */
public abstract class BasePresenter<V extends IBaseView> implements IPresenter<V>, ILifeCycle, LifecycleObserver {

    public V mView;
    private Lifecycle lifecycle;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BasePresenter(V mView,Lifecycle lifecycle) {
        this.mView = mView;
        this.lifecycle= lifecycle;
    }

    @Override
    public void attachView(V mView, Lifecycle lifecycle) {
        this.mView = mView;
        lifecycle.addObserver(this);
    }

    @Override
    public void detachView() {
        mView = null;
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public void addSubscription(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        detachView();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }
}
