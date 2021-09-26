package com.basis.mvp;

/**
 * @author gyn
 * @date 2021/9/24
 */
public interface IBaseView {

    //    void showLoading(String msg);
//
//    void dismissLoading();
    void showWaitingDialog();

    void hideWaitingDialog();

    void showLoadingView();

    void showNormal();

    void showEmpty();

    void showError(int errorCode, String message);

    void showError(String message);

    void onLogout();

    void showMessage(String message);
}
