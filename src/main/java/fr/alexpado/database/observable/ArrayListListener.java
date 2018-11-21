package fr.alexpado.database.observable;

public interface ArrayListListener<T> {

    void onItemAdded(T item);
    void onItemDeleted(T item);

}
