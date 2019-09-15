package com.chesire.malime.flow.series.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.chesire.lifecyklelog.LogLifecykle
import com.chesire.malime.R
import com.chesire.malime.core.flags.UserSeriesStatus
import com.chesire.malime.core.models.SeriesModel
import com.chesire.malime.extensions.extraNotNull
import com.chesire.malime.flow.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.view_series_detail_header.seriesDetailHeaderSubtype
import kotlinx.android.synthetic.main.view_series_detail_header.seriesDetailHeaderTitle
import kotlinx.android.synthetic.main.view_series_detail_header.seriesDetailHeaderType
import kotlinx.android.synthetic.main.view_series_detail_series_status.seriesDetailStatusGroup
import timber.log.Timber
import javax.inject.Inject

@LogLifecykle
class SeriesDetailSheetFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)
            .get<SeriesDetailViewModel>()
            .also { viewModel ->
                viewModel.setModel(seriesModel)
            }
    }
    private val seriesModel by extraNotNull<SeriesModel>(MODEL_BUNDLE_ID, null)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_series_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        with(viewModel.mutableModel) {
            seriesDetailHeaderTitle.text = seriesName
            seriesDetailHeaderType.text = seriesType
            seriesDetailHeaderSubtype.text = seriesSubType
            setupSeriesStatusListener(this)
            setupInitialSeriesStatus(this)
        }
    }

    private fun setupSeriesStatusListener(model: MutableSeriesModel) {
        var lastCheckedId = View.NO_ID
        seriesDetailStatusGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == View.NO_ID) {
                Timber.w("Tried to uncheck chip, resetting to be checked")
                group.check(lastCheckedId)
                return@setOnCheckedChangeListener
            }
            lastCheckedId = checkedId
            Timber.d("Chip checked, selected is now $checkedId")

            when (checkedId) {
                R.id.seriesDetailChipCurrent -> model.userSeriesStatus = UserSeriesStatus.Current
                R.id.seriesDetailChipCompleted ->
                    model.userSeriesStatus = UserSeriesStatus.Completed
                R.id.seriesDetailChipDropped -> model.userSeriesStatus = UserSeriesStatus.Dropped
                R.id.seriesDetailChipOnHold -> model.userSeriesStatus = UserSeriesStatus.OnHold
                R.id.seriesDetailChipPlanned -> model.userSeriesStatus = UserSeriesStatus.Planned
            }
        }
    }

    private fun setupInitialSeriesStatus(model: MutableSeriesModel) {
        seriesDetailStatusGroup.check(
            when (model.userSeriesStatus) {
                UserSeriesStatus.Current -> R.id.seriesDetailChipCurrent
                UserSeriesStatus.Completed -> R.id.seriesDetailChipCompleted
                UserSeriesStatus.Dropped -> R.id.seriesDetailChipDropped
                UserSeriesStatus.OnHold -> R.id.seriesDetailChipOnHold
                UserSeriesStatus.Planned -> R.id.seriesDetailChipPlanned
                else -> 0
            }
        )
    }

    companion object {
        const val TAG = "SeriesDetailSheetFragment"
        private const val MODEL_BUNDLE_ID = "SeriesDetailSheetFragment_model"

        /**
         * Creates a new instance of the [SeriesDetailSheetFragment], using the [seriesModel] for
         * data.
         */
        fun newInstance(seriesModel: SeriesModel): SeriesDetailSheetFragment {
            return SeriesDetailSheetFragment().apply {
                arguments = bundleOf(MODEL_BUNDLE_ID to seriesModel)
            }
        }
    }
}
