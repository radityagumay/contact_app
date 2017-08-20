package net.radityalabs.contactapp.presentation.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import net.radityalabs.contactapp.ContactApp;
import net.radityalabs.contactapp.R;
import net.radityalabs.contactapp.data.network.response.ContactDetailResponse;
import net.radityalabs.contactapp.domain.model.ContactDetailInfoModel;
import net.radityalabs.contactapp.domain.usecase.ContactDetailUseCase;
import net.radityalabs.contactapp.presentation.factory.ToastFactory;
import net.radityalabs.contactapp.presentation.manager.RClipBoardManager;
import net.radityalabs.contactapp.presentation.presenter.contract.ContactDetailContract;
import net.radityalabs.contactapp.presentation.rx.RxPresenter;
import net.radityalabs.contactapp.presentation.util.PhoneUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radityagumay on 4/13/17.
 */

public class ContactDetailPresenter extends RxPresenter<ContactDetailContract.View> implements
        ContactDetailContract.Presenter {

    private static final String TAG = ContactDetailPresenter.class.getSimpleName();

    private Context mContext;
    private ContactDetailUseCase useCase;

    @Inject
    public ContactDetailPresenter(ContactDetailUseCase useCase, ContactApp context) {
        this.useCase = useCase;
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void getContactDetail(int userId) {
        Disposable disposable = useCase.getDetailContact(userId)
                .subscribe(new Consumer<ContactDetailResponse>() {
                    @Override
                    public void accept(ContactDetailResponse contactDetailResponse) throws Exception {
                        Log.d(TAG, "getContactDetail: Success");
                        mView.showContactDetail(contactDetailResponse);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "getContactDetail: " + throwable.getMessage(), throwable);
                    }
                });
        addDisposable(disposable);
    }

    public List<ContactDetailInfoModel> getInfoBuilder(ContactDetailResponse contactDetailResponse) {
        List<ContactDetailInfoModel> contacts = new ArrayList<>(2);

        ContactDetailInfoModel phoneModel = new ContactDetailInfoModel();
        phoneModel.leftIcon = R.mipmap.ic_call_blue;
        phoneModel.rightIcon = R.mipmap.ic_message;
        phoneModel.isRightIconSet = true;
        phoneModel.bodyOne = contactDetailResponse.phoneNumber;
        phoneModel.bodyTwo = "Mobile";
        contacts.add(phoneModel);

        ContactDetailInfoModel email = new ContactDetailInfoModel();
        email.leftIcon = R.mipmap.ic_email_blue;
        email.isRightIconSet = false;
        email.bodyOne = contactDetailResponse.email;
        email.bodyTwo = "Home";
        contacts.add(email);
        return contacts;
    }

    public void composeOnClick(View view, int position, ContactDetailInfoModel contact, boolean isLongPressed) {
        switch (position) {
            case 0: {
                switch (view.getId()) {
                    case R.id.iv_left_icon: {
                        if (!isLongPressed) {
                            Intent intent = PhoneUtil.call(contact.bodyOne);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                mContext.startActivity(intent);
                            }
                        } else {
                            copyToClipBoard(contact.bodyOne);
                        }
                    }
                    break;
                    case R.id.iv_right_icon: {
                        if (!isLongPressed) {
                            Intent intent = PhoneUtil.sms(contact.bodyOne);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                mContext.startActivity(intent);
                            }
                        } else {
                            copyToClipBoard(contact.bodyOne);
                        }
                    }
                    break;
                }
            }
            break;
            case 1: {
                switch (view.getId()) {
                    case R.id.iv_left_icon: {
                        if (!isLongPressed) {
                            Intent intent = PhoneUtil.email(contact.bodyOne);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                mContext.startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                            }
                        } else {
                            copyToClipBoard(contact.bodyOne);
                        }
                    }
                    break;
                }
            }
            break;
        }
    }

    private void copyToClipBoard(String message) {
        boolean isSuccess = RClipBoardManager.copyToClipboard(mContext, message);
        StringBuilder builder = new StringBuilder();
        builder.append(message).append(" ")
                .append(isSuccess ? "Berhasil disalin" : "Gagal disalin");
        ToastFactory.show(mContext, builder.toString());
    }
}