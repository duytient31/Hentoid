package me.devsaki.hentoid.fragments.viewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.database.domains.ImageFile;
import me.devsaki.hentoid.viewholders.ImageFileItem;
import me.devsaki.hentoid.viewmodels.ImageViewerViewModel;

import static androidx.core.view.ViewCompat.requireViewById;

public class ImageGalleryFragment extends Fragment {

    private static final String KEY_FILTER_FAVOURITES = "filter_favourites";

    private final ItemAdapter<ImageFileItem> itemAdapter = new ItemAdapter<>();
    private final FastAdapter<ImageFileItem> fastAdapter = FastAdapter.with(itemAdapter);

    private ImageViewerViewModel viewModel;
    private MenuItem showFavouritePagesButton;
    private RecyclerView recyclerView;

    private int startIndex = 0;

    private boolean filterFavourites = false;


    static ImageGalleryFragment newInstance(boolean filterFavourites) {
        ImageGalleryFragment fragment = new ImageGalleryFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_FILTER_FAVOURITES, filterFavourites);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_viewer_gallery, container, false);

        Bundle arguments = getArguments();
        if (arguments != null)
            filterFavourites = arguments.getBoolean(KEY_FILTER_FAVOURITES, false);

        setHasOptionsMenu(true);

        // Item click listener
        fastAdapter.setOnClickListener((v, a, i, p) -> onItemClick(p));
        // Favourite button click listener
        fastAdapter.addEventHook(new ClickEventHook<ImageFileItem>() {
            @Override
            public void onClick(@NotNull View view, int i, @NotNull FastAdapter<ImageFileItem> fastAdapter, @NotNull ImageFileItem item) {
                onFavouriteClick(item.getImage());
            }

            @Nullable
            @Override
            public View onBind(RecyclerView.@NotNull ViewHolder viewHolder) {
                if (viewHolder instanceof ImageFileItem.ImageViewHolder) {
                    return ((ImageFileItem.ImageViewHolder) viewHolder).getFavouriteButton();
                }
                return super.onBind(viewHolder);
            }
        });

        // Filtering
        itemAdapter.getItemFilter().setFilterPredicate((imageFileItem, charSequence) -> !charSequence.equals("true") || imageFileItem.isFavourite());

        recyclerView = requireViewById(rootView, R.id.viewer_gallery_recycler);
        recyclerView.setAdapter(fastAdapter);

        Toolbar toolbar = requireViewById(rootView, R.id.viewer_gallery_toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        toolbar.setOnMenuItemClickListener(clickedMenuItem -> {
            if (clickedMenuItem.getItemId() == R.id.action_show_favorite_pages) {
                toggleFavouritesDisplay();
            }
            return true;
        });
        showFavouritePagesButton = toolbar.getMenu().findItem(R.id.action_show_favorite_pages);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ImageViewerViewModel.class);
        viewModel.getStartingIndex().observe(getViewLifecycleOwner(), this::onStartingIndexChanged);
        viewModel.getImages().observe(getViewLifecycleOwner(), this::onImagesChanged);
    }

    private void onImagesChanged(List<ImageFile> images) {
        for (ImageFile img : images) {
            ImageFileItem holder = new ImageFileItem(img);
            if (startIndex == img.getDisplayOrder()) holder.setCurrent(true);
            itemAdapter.add(holder);
        }
        updateListFilter();
        updateFavouriteDisplay();
    }

    private void onStartingIndexChanged(Integer startingIndex) {
        startIndex = startingIndex;
    }

    private boolean onItemClick(int position) {
        ImageFileItem imgFile = itemAdapter.getAdapterItem(position);
        viewModel.setStartingIndex(imgFile.getImage().getDisplayOrder());
        requireActivity().onBackPressed();
        return true;
    }

    private void onFavouriteClick(ImageFile img) {
        viewModel.togglePageFavourite(img, this::onFavouriteSuccess);
    }

    private void onFavouriteSuccess(ImageFile img) {
        if (filterFavourites) {
            // Reset favs filter if no favourite page remains
            if (!hasFavourite()) {
                filterFavourites = false;
                itemAdapter.filter("");
                if (itemAdapter.getAdapterItemCount() > 0)
                    recyclerView.scrollToPosition(0);
            } else {
                fastAdapter.notifyDataSetChanged(); // Because no easy way to spot which item has changed when the view is filtered
            }
        } else fastAdapter.notifyItemChanged(img.getDisplayOrder());

        showFavouritePagesButton.setVisible(hasFavourite());
    }

    private void toggleFavouritesDisplay() {
        filterFavourites = !filterFavourites;
        updateFavouriteDisplay();
        updateListFilter();
    }

    private void updateFavouriteDisplay() {
        showFavouritePagesButton.setVisible(hasFavourite());
        showFavouritePagesButton.setIcon(filterFavourites ? R.drawable.ic_fav_full : R.drawable.ic_fav_empty);
    }

    private void updateListFilter() {
        if (itemAdapter.getAdapterItemCount() > 0) {
            itemAdapter.filter(filterFavourites ? "true" : "");
            if (itemAdapter.getAdapterItemCount() > startIndex)
                recyclerView.scrollToPosition(startIndex);
            else recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FILTER_FAVOURITES, filterFavourites);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            filterFavourites = savedInstanceState.getBoolean(KEY_FILTER_FAVOURITES, false);
    }

    private boolean hasFavourite() {
        List<ImageFileItem> images = itemAdapter.getAdapterItems();
        for (ImageFileItem item : images) if (item.isFavourite()) return true;
        return false;
    }
}
