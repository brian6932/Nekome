package com.chesire.malime.view.maldisplay

import android.content.SharedPreferences
import android.databinding.ViewDataBinding
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.chesire.malime.BR
import com.chesire.malime.R
import com.chesire.malime.core.models.MalimeModel
import com.chesire.malime.databinding.ItemMalmodelBinding
import com.chesire.malime.util.GlideApp
import com.chesire.malime.util.preferenceFilter
import com.chesire.malime.util.preferenceSort
import kotlinx.android.synthetic.main.item_malmodel.view.item_malmodel_content_layout
import kotlinx.android.synthetic.main.item_malmodel.view.item_malmodel_image
import kotlinx.android.synthetic.main.item_malmodel.view.item_malmodel_loading_layout
import kotlinx.android.synthetic.main.item_malmodel.view.item_malmodel_neg_one
import kotlinx.android.synthetic.main.item_malmodel.view.item_malmodel_plus_one
import timber.log.Timber

class MalDisplayViewAdapter(
    private val listener: ModelInteractionListener
) : RecyclerView.Adapter<MalDisplayViewAdapter.ViewHolder>(), Filterable,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val items = ArrayList<MalimeModel>()
    private val filteredItems = ArrayList<MalimeModel>()
    private val filter = MalDisplayFilter()

    fun addAll(newItems: List<MalimeModel>) {
        items.clear()
        items.addAll(newItems)
        filter.filter("")
    }

    fun clear(item: MalimeModel) {
        val foundItem = items.find { it.seriesId == item.seriesId }
        items.remove(foundItem)
        filter.filter("")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (items.count() == 0) {
            // we should show a screen with no items
            // for now return the same item
            return ViewHolder(
                ItemMalmodelBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ViewHolder(
                ItemMalmodelBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return filteredItems.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }

    override fun getFilter(): Filter = filter

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            if (it.contains(preferenceSort) || it.contains(preferenceFilter)) {
                filter.filter("")
            }
        }
    }

    inner class ViewHolder(
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val loadingLayout = binding.root.item_malmodel_loading_layout
        private val contentLayout = binding.root.item_malmodel_content_layout

        fun bind(item: MalimeModel?) {
            if (item == null) {
                Timber.w("Empty list found, not performing binding")
                return
            }

            binding.setVariable(BR.model, item)
            binding.executePendingBindings()

            GlideApp.with(binding.root)
                .load(
                    if (item.posterImage == "") {
                        item.coverImage
                    } else {
                        item.posterImage
                    }
                )
                .into(binding.root.item_malmodel_image)


            binding.root.apply {
                item_malmodel_neg_one.setOnClickListener {
                    updateSeriesProgress(item, item.progress - 1)
                }
                item_malmodel_plus_one.setOnClickListener {
                    updateSeriesProgress(item, item.progress + 1)
                }
                item_malmodel_image.setOnClickListener {
                    listener.onImageClicked(item)
                }
            }
        }

        private fun updateSeriesProgress(item: MalimeModel, newProgress: Int) {
            setLayoutState(false)

            listener.onSeriesSetProgress(item, newProgress, { success ->
                setLayoutState(true)
                if (!success) {
                    Snackbar.make(
                        loadingLayout,
                        String.format(
                            binding.root.context.getString(R.string.malitem_update_series_failure),
                            item.title
                        ), Snackbar.LENGTH_LONG
                    ).show()
                }
            })
        }

        private fun setLayoutState(enabled: Boolean) {
            loadingLayout.visibility = if (enabled) {
                View.GONE
            } else {
                View.VISIBLE
            }
            contentLayout.isEnabled = enabled
            for (i in 0 until contentLayout.childCount) {
                contentLayout.getChildAt(i).isEnabled = enabled
            }
        }
    }

    inner class MalDisplayFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}