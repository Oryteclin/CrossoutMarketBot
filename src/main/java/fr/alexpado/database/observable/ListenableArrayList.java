package fr.alexpado.database.observable;

import java.util.ArrayList;
import java.util.Collection;

public class ListenableArrayList<T> extends ArrayList<T> {

    private ArrayListListener<T> listener;

    public ListenableArrayList() {
        super();
    }

    public ListenableArrayList(ArrayList<T> list) {
        this.addAll(list);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if(listener != null) c.forEach(listener::onItemAdded);
        return super.addAll(c);
    }

    @Override
    public boolean add(T t) {
        if(listener != null) listener.onItemAdded(t);
        return super.add(t);
    }

    @Override
    public T remove(int index) {
        T item = this.get(index);
        if(listener != null) listener.onItemDeleted(item);
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        if(this.contains(o) && listener != null) {
            listener.onItemDeleted(((T) o));
        }
        return super.remove(o);
    }

    public void setListener(ArrayListListener<T> listener) {
        this.listener = listener;
    }
}
