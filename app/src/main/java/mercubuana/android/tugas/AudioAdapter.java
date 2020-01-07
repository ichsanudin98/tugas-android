package mercubuana.android.tugas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {
    private List<AudioModel> audioEntities;

    {
        audioEntities = new ArrayList<>();
    }

    private AudioListener listener;

    public AudioAdapter(List<AudioModel> audioEntities, AudioListener listener) {
        super();
        this.listener = listener;
        this.audioEntities = audioEntities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel audioEntity = audioEntities.get(position);

        if (audioEntity != null)
            holder.txvName.setText(audioEntity.getName());

        holder.layoutParent.setOnClickListener(v -> listener.onAudioClick(audioEntity));
    }

    @Override
    public int getItemCount() {
        return audioEntities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayoutCompat layoutParent;
        AppCompatTextView txvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutParent = itemView.findViewById(R.id.item_parent);
            txvName = itemView.findViewById(R.id.item_content);
        }
    }

    public void addAudio(AudioModel data) {
        if (data != null)
            audioEntities.add(data);
    }
}

interface AudioListener {
    void onAudioClick(AudioModel audioModel);
}