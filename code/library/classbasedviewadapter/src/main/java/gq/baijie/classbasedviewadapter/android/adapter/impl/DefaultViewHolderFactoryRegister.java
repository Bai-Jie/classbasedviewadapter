package gq.baijie.classbasedviewadapter.android.adapter.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import gq.baijie.classbasedviewadapter.android.adapter.ViewHolderFactory;
import gq.baijie.classbasedviewadapter.android.adapter.ViewHolderFactoryRegister;

public class DefaultViewHolderFactoryRegister implements ViewHolderFactoryRegister {

    private int counter = 1;

    private Map<Class, ViewHolderFactoryWrapper> factories = new HashMap<>();
    private Map<Integer, ViewHolderFactoryWrapper> idToFactories = new HashMap<>();

    @Override
    public void registerViewHolderFactory(ViewHolderFactory factory) {
        final int id = counter++;
        final Class type = factory.forClass();

        final ViewHolderFactoryWrapper factoryWrapper = new ViewHolderFactoryWrapper(id, factory);
        final ViewHolderFactoryWrapper oldOne = factories.put(type, factoryWrapper);
        if (oldOne != null) {
            idToFactories.remove(oldOne.id);
        }
        idToFactories.put(id, factoryWrapper);
    }

    @Override
    public int getItemViewType(Class itemType) {
        return searchForNonNull(itemType).id;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int itemViewType) {
        return idToFactories.get(itemViewType).viewHolderFactory.createViewHolder(parent);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder holder, Object forThis, int position) {
        searchForNonNull(forThis.getClass()).viewHolderFactory
                .bindViewHolder(holder, forThis, position);
    }

    @NonNull
    private ViewHolderFactoryWrapper searchForNonNull(Class clazz) {
        ViewHolderFactoryWrapper candidate = searchFor(clazz);
        if (candidate == null) {
            throw new IllegalStateException("Please set ViewHolderFactory before use it.");
        }
        return candidate;
    }

    @Nullable
    private ViewHolderFactoryWrapper searchFor(Class clazz) {
        ViewHolderFactoryWrapper candidate = factories.get(clazz);
        if (candidate != null) {
            return candidate;
        }
        for (Class superClass = clazz.getSuperclass();
                superClass != null;
                superClass = clazz.getSuperclass()) {
            candidate = factories.get(superClass);
            if (candidate != null) {
                return candidate;
            }
        }
        for (Class interfaceClass : clazz.getInterfaces()) {
            candidate = factories.get(interfaceClass);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }


    private static class ViewHolderFactoryWrapper {

        private final int id;

        @NonNull
        private final ViewHolderFactory viewHolderFactory;

        private ViewHolderFactoryWrapper(int id, @NonNull ViewHolderFactory viewHolderFactory) {
            this.id = id;
            this.viewHolderFactory = viewHolderFactory;
        }
    }

}
