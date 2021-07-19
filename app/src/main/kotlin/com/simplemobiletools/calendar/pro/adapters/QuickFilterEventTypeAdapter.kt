package com.simplemobiletools.calendar.pro.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.extensions.config
import com.simplemobiletools.calendar.pro.models.EventType
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.helpers.MEDIUM_ALPHA
import kotlinx.android.synthetic.main.quick_filter_event_type_view.view.*
import java.util.*

class QuickFilterEventTypeAdapter(val activity: SimpleActivity, val allEventTypes: List<EventType>, private val quickFilterEventTypeIds: Set<String>, val filterChanged: () -> Unit) :
    RecyclerView.Adapter<QuickFilterEventTypeAdapter.ViewHolder>() {
    private val activeKeys = HashSet<Long>()
    private val quickFilterEventTypes = ArrayList<EventType>()
    private val displayEventTypes = activity.config.displayEventTypes

    private val textColorActive = activity.config.textColor
    private val textColorInactive = textColorActive.adjustAlpha(MEDIUM_ALPHA)

    private val minItemWidth = activity.resources.getDimensionPixelSize(R.dimen.quick_filter_min_width)

    init {
        quickFilterEventTypeIds.forEach { quickFilterEventType ->
            // Find the associated eventType, return if none found
            val eventType = allEventTypes.find { eventType -> eventType.id.toString() == quickFilterEventType }
                ?: return@forEach
            quickFilterEventTypes.add(eventType)

            // Check if it is currently active
            if (displayEventTypes.contains(eventType.id.toString())) {
                activeKeys.add(eventType.id!!)
            }
        }
    }

    private fun toggleItemSelection(select: Boolean, eventType: EventType, pos: Int) {
        if (select) {
            activeKeys.add(eventType.id!!)
        } else {
            activeKeys.remove(eventType.id)
        }

        notifyItemChanged(pos)
    }

    fun getSelectedItemsList() = activeKeys.asSequence().map { it }.toMutableList() as ArrayList<Long>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val parentWidth = parent.measuredWidth
        val nrOfItems = quickFilterEventTypes.size
        val view = activity.layoutInflater.inflate(R.layout.quick_filter_event_type_view, parent, false)
        if (nrOfItems * minItemWidth > parentWidth) view.layoutParams.width = minItemWidth
        else view.layoutParams.width = parentWidth / nrOfItems
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventType = quickFilterEventTypes[position]
        holder.bindView(eventType)
    }

    override fun getItemCount() = quickFilterEventTypes.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(eventType: EventType): View {
            val isSelected = activeKeys.contains(eventType.id)
            itemView.apply {
                quick_filter_event_type.text = eventType.title
                val textColor = if (isSelected) textColorActive else textColorInactive
                quick_filter_event_type.setTextColor(textColor)
                val indicatorHeight =
                    if (isSelected) resources.getDimensionPixelSize(R.dimen.quick_filter_active_line_size)
                    else resources.getDimensionPixelSize(R.dimen.quick_filter_inactive_line_size)
                quick_filter_event_type_color.layoutParams.height = indicatorHeight
                quick_filter_event_type_color.setBackgroundColor(eventType.color)
                quick_filter_event_type.setOnClickListener {
                    viewClicked(!isSelected, eventType)
                    filterChanged()
                }
            }

            return itemView
        }

        private fun viewClicked(select: Boolean, eventType: EventType) {
            if (select)
                activity.config.displayEventTypes = activity.config.displayEventTypes.plus(eventType.id.toString())
            else
                activity.config.displayEventTypes = activity.config.displayEventTypes.minus(eventType.id.toString())
            toggleItemSelection(select, eventType, adapterPosition)
        }
    }
}
