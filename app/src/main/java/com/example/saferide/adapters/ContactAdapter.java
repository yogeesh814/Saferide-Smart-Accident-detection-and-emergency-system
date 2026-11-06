package com.example.saferide.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saferide.R;
import com.example.saferide.models.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    public interface ContactActionListener {
        void onEdit(ContactModel contact);

        void onDelete(ContactModel contact);
    }

    private final List<ContactModel> contacts = new ArrayList<>();
    private final ContactActionListener listener;

    public ContactAdapter(ContactActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ContactModel> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());
        holder.relation.setText(contact.getRelation());
        holder.editButton.setOnClickListener(v -> listener.onEdit(contact));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView phone;
        TextView relation;
        ImageButton editButton;
        ImageButton deleteButton;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            phone = itemView.findViewById(R.id.contactPhone);
            relation = itemView.findViewById(R.id.contactRelation);
            editButton = itemView.findViewById(R.id.editContactButton);
            deleteButton = itemView.findViewById(R.id.deleteContactButton);
        }
    }
}
