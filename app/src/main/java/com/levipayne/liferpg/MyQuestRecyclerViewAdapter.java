package com.levipayne.liferpg;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link} and makes a call to the
 * specified {@link MainActivity}.
 */
public class MyQuestRecyclerViewAdapter extends RecyclerView.Adapter<MyQuestRecyclerViewAdapter.ViewHolder> {

    private final List<Quest> mValues;
    private final MainActivity mListener;

    public MyQuestRecyclerViewAdapter(List<Quest> items, MainActivity listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mQuest = mValues.get(position);
        holder.mDescriptionView.setText(mValues.get(position).description);
        holder.mDifficultyView.setText(mValues.get(position).difficulty);
        holder.mGoldView.setText(String.valueOf(mValues.get(position).reward));

        switch (mValues.get(position).difficulty) {
            case Quest.EASY_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.easyColor));
                break;
            case Quest.MEDIUM_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.mediumColor));
                break;
            case Quest.HARD_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.hardColor));
                break;
            case Quest.LEGENDARY_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.legendaryColor));
                break;
            default:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.easyColor));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mQuest);
                }
            }
        });
    }

    public void addItem(Quest q) {
        mValues.add(q);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDescriptionView;
        public final TextView mDifficultyView;
        public final TextView mGoldView;
        public Quest mQuest;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mDifficultyView = (TextView) view.findViewById(R.id.difficulty);
            mGoldView = (TextView) view.findViewById(R.id.g);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDifficultyView.getText() + "'";
        }
    }
}
