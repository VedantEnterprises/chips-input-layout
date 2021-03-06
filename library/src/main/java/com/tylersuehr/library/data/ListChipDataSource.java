package com.tylersuehr.library.data;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright © 2017 Tyler Suehr
 *
 * Simple implementation of {@link ChipDataSource} that will aggregate chips
 * using an {@link ArrayList}.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
public class ListChipDataSource implements ChipDataSource {
    /* Aggregation of changeObservers to watch changes to chip selection */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<ChipSelectionObserver> selectionObservers;

    /* Aggregation of changeObservers to watch changes to data source */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<ChipChangedObserver> changeObservers;

    /* Stores all the original chips */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> originalChips;

    /* Stores all the filtered chips, that are not selected by the user */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> filteredChips;

    /* Stores all the selected chips, selected by the user */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    List<Chip> selectedChips;


    /* Construct with all empty lists */
    public ListChipDataSource() {
        this.originalChips = new ArrayList<>();
        this.filteredChips = new ArrayList<>();
        this.selectedChips = new ArrayList<>();
    }

    @Override
    public List<Chip> getSelectedChips() {
        return selectedChips;
    }

    @Override
    public List<Chip> getFilteredChips() {
        return filteredChips;
    }

    @Override
    public List<Chip> getOriginalChips() {
        return originalChips;
    }

    @Override
    public Chip getFilteredChip(int position) {
        return filteredChips.get(position);
    }

    @Override
    public Chip getSelectedChip(int position) {
        return selectedChips.get(position);
    }

    @Override
    public void setFilterableChips(List<? extends Chip> chips) {
        if (chips == null) {
            throw new NullPointerException("Chips cannot be null!");
        }

        // Instantiate our chip lists with the size of the given list
        this.originalChips = new ArrayList<>(chips.size());
        this.filteredChips = new ArrayList<>(chips.size());
        this.selectedChips = new ArrayList<>(chips.size());

        // Only copy the data from our chips into the original and filtered lists
        for (Chip chip : chips) {
            chip.setFilterable(true);
            this.originalChips.add(chip);
            this.filteredChips.add(chip);
        }

        // Sort the lists
        Collections.sort(originalChips, Chip.getComparator());
        Collections.sort(filteredChips, Chip.getComparator());
    }

    @Override
    public void createFilteredChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }
        chip.setFilterable(true);
        this.originalChips.add(chip);
        this.filteredChips.add(chip);
        notifyDataSourceChanged();
    }

    @Override
    public void createSelectedChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }
        this.selectedChips.add(chip);
        notifyChipSelected(chip);
        notifyDataSourceChanged();
    }

    @Override
    public void takeChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }

        // Check if chip is filterable
        if (chip.isFilterable()) {
            // Check if chip is actually in the filtered list
            if (filteredChips.contains(chip)) {
                this.originalChips.remove(chip);
                this.filteredChips.remove(chip);
                this.selectedChips.add(chip);
            } else {
                throw new IllegalArgumentException("Chip is not in filtered chip list!");
            }
        } else {
            throw new IllegalArgumentException("Cannot take a non-filterable chip!");
        }

        notifyChipSelected(chip);
        notifyDataSourceChanged();
    }

    @Override
    public void takeChip(int position) {
        final Chip foundChip = filteredChips.get(position);
        if (foundChip == null) {
            throw new NullPointerException("Chip cannot be null; not found in filtered chip list!");
        }

        // Check if chip is filterable
        if (foundChip.isFilterable()) {
            // Since the child isn't null, we know it's in the filtered list
            this.originalChips.remove(foundChip);
            this.filteredChips.remove(foundChip);
            this.selectedChips.add(foundChip);
        } else {
            // Just add it to the selected list only
            this.selectedChips.add(foundChip);
        }

        notifyChipSelected(foundChip);
        notifyDataSourceChanged();
    }

    @Override
    public void replaceChip(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }

        // Check if chip is actually selected
        if (selectedChips.contains(chip)) {
            this.selectedChips.remove(chip);

            // Check if the chip is filterable
            if (chip.isFilterable()) {
                this.filteredChips.add(chip);
                this.originalChips.add(chip);
            }

            notifyChipUnselected(chip);
            notifyDataSourceChanged();
        } else {
            throw new IllegalArgumentException("Chip is not in selected chip list!");
        }
    }

    @Override
    public void replaceChip(int position) {
        final Chip foundChip = selectedChips.get(position);
        if (foundChip == null) {
            throw new NullPointerException("Chip cannot be null; not found in selected chip list!");
        }

        // Since not null, we know the chip is selected
        this.selectedChips.remove(foundChip);

        // Check if the chip is filterable
        if (foundChip.isFilterable()) {
            this.filteredChips.add(foundChip);
            this.originalChips.add(foundChip);
        }

        notifyChipUnselected(foundChip);
        notifyDataSourceChanged();
    }

    @Override
    public boolean existsInDataSource(Chip chip) {
        if (chip == null) {
            throw new NullPointerException("Chip cannot be null!");
        }
        return (originalChips.contains(chip)
                || filteredChips.contains(chip)
                || selectedChips.contains(chip));
    }

    @Override
    public void addChipSelectionObserver(ChipSelectionObserver observer) {
        if (observer == null) {
            throw new NullPointerException("ChipSelectionObserver cannot be null!");
        }
        if (selectionObservers == null) {
            this.selectionObservers = new LinkedList<>();
        }
        this.selectionObservers.add(observer);
    }

    @Override
    public void removeChipSelectionObserver(ChipSelectionObserver observer) {
        if (observer == null) {
            throw new NullPointerException("ChipSelectionObserver cannot be null!");
        }
        if (selectionObservers != null) {
            this.selectionObservers.remove(observer);
        }
    }

    @Override
    public void addChipChangedObserver(ChipChangedObserver observer) {
        if (observer == null) {
            throw new NullPointerException("ChipChangedObserver cannot be null!");
        }
        if (changeObservers == null) {
            this.changeObservers = new LinkedList<>();
        }
        this.changeObservers.add(observer);
    }

    @Override
    public void removeChipChangedObserver(ChipChangedObserver observer) {
        if (observer == null) {
            throw new NullPointerException("ChipChangedObserver cannot be null!");
        }
        if (changeObservers != null) {
            this.changeObservers.remove(observer);
        }
    }

    @Override
    public void removeAllObservers() {
        if (selectionObservers != null) {
            this.selectionObservers.clear();
            this.selectionObservers = null;
        }
        if (changeObservers != null) {
            this.changeObservers.clear();
            this.changeObservers = null;
        }
    }

    /**
     * Notifies {@link #changeObservers} that a change to the data source happened.
     */
    private void notifyDataSourceChanged() {
        if (changeObservers != null) {
            synchronized (this) {
                for (ChipChangedObserver ob : changeObservers) {
                    ob.onChipDataSourceChanged();
                }
            }
        }
    }

    /**
     * Notifies {@link #selectionObservers} that a chip was selected in the data source.
     * @param chip {@link Chip} selected
     */
    private void notifyChipSelected(Chip chip) {
        if (selectionObservers != null) {
            synchronized (this) {
                for (ChipSelectionObserver ob : selectionObservers) {
                    ob.onChipSelected(chip);
                }
            }
        }
    }

    /**
     * Notifies {@link #selectionObservers} that a chip was unselected in the data source.
     * @param chip {@link Chip} unselected
     */
    private void notifyChipUnselected(Chip chip) {
        if (selectionObservers != null) {
            synchronized (this) {
                for (ChipSelectionObserver ob : selectionObservers) {
                    ob.onChipDeselected(chip);
                }
            }
        }
    }
}