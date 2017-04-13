package net.radityalabs.contactapp.domain.usecase;

import android.content.Context;
import android.util.Log;

import net.radityalabs.contactapp.data.network.RestService;
import net.radityalabs.contactapp.data.network.RetrofitHelper;
import net.radityalabs.contactapp.data.network.response.ContactListResponse;
import net.radityalabs.contactapp.data.realm.RealmHelper;
import net.radityalabs.contactapp.data.realm.table.ContactObject;
import net.radityalabs.contactapp.presentation.rx.RxUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by radityagumay on 4/13/17.
 */

public class ContactListFragmentUseCase {

    private static final String TAG = ContactListFragmentUseCase.class.getSimpleName();

    private RealmHelper realmHelper;
    private RestService service;
    private Context context;

    public ContactListFragmentUseCase(RetrofitHelper retrofitHelper, RealmHelper realmHelper, Context context) {
        this.service = retrofitHelper.getRestService();
        this.realmHelper = realmHelper;
        this.context = context;
    }

    public Flowable<List<ContactListResponse>> getContactListApi() {
        return service.getContactList()
                .compose(RxUtil.<List<ContactListResponse>>single())
                .compose(new SingleTransformer<List<ContactListResponse>, List<ContactListResponse>>() {
                    @Override
                    public SingleSource<List<ContactListResponse>> apply(Single<List<ContactListResponse>> upstream) {
                        return upstream.doOnSuccess(new Consumer<List<ContactListResponse>>() {
                            @Override
                            public void accept(List<ContactListResponse> responses) throws Exception {
                                insertContact(responses);
                            }
                        });
                    }
                }).toFlowable();
    }

    public Flowable<List<ContactListResponse>> getContactListDb() {
        return Single.create(new SingleOnSubscribe<List<ContactObject>>() {
            @Override
            public void subscribe(SingleEmitter<List<ContactObject>> emmit) throws Exception {
                Realm realm = Realm.getInstance(realmHelper.buildRealmConfiguration());
                List<ContactObject> contacts = realm.copyFromRealm(realm.where(ContactObject.class).findAll());
                emmit.onSuccess(contacts.size() > 0 ? contacts : new ArrayList<ContactObject>());
                realm.close();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<ContactObject>, List<ContactListResponse>>() {
                    @Override
                    public List<ContactListResponse> apply(List<ContactObject> contacts) throws Exception {
                        List<ContactListResponse> response = new ArrayList<>(contacts.size());
                        for (int i = 0; i < contacts.size(); i++) {
                            ContactListResponse obj = new ContactListResponse();
                            obj.id = (int) contacts.get(i).id;
                            obj.firstName = contacts.get(i).firstName;
                            obj.lastName = contacts.get(i).lastName;
                            obj.profilePic = contacts.get(i).profilePic;
                            obj.isFavorite = contacts.get(i).isFavorite;
                            obj.detailUrl = contacts.get(i).detailUrl;
                            response.add(obj);
                        }
                        return response;
                    }
                }).toFlowable();
    }

    private void insertContact(final List<ContactListResponse> responses) {
        Single.create(new SingleOnSubscribe<List<ContactListResponse>>() {
            @Override
            public void subscribe(SingleEmitter<List<ContactListResponse>> e) throws Exception {
                final RealmList<ContactObject> contacts = new RealmList<>();
                int size = responses.size();
                for (int i = 0; i < size; i++) {
                    ContactObject obj = new ContactObject();
                    obj.id = responses.get(i).id;
                    obj.firstName = responses.get(i).firstName;
                    obj.lastName = responses.get(i).lastName;
                    obj.profilePic = responses.get(i).profilePic;
                    obj.isFavorite = responses.get(i).isFavorite;
                    obj.detailUrl = responses.get(i).detailUrl;
                    contacts.add(obj);
                }

                Realm realm = Realm.getInstance(realmHelper.buildRealmConfiguration());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(contacts);
                    }
                });
                realm.close();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ContactListResponse>>() {
                    @Override
                    public void accept(List<ContactListResponse> responses) throws Exception {
                        Log.d(TAG, "Insert to database: " + responses.size());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage(), throwable);
                    }
                });
    }
}
