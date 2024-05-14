package ai.robominder.bt_devices.bluetooth

import ai.robominder.bt_devices.R
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


class BluetoothRecyclerAdapter :
    RecyclerView.Adapter<BluetoothRecyclerAdapter.ItemViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bluetooth_item, parent,false)
        return ItemViewHolder(view)
    }

    private val differCallback = object : DiffUtil.ItemCallback<BluetoothDeviceItem>(){
        override fun areItemsTheSame(oldItem: BluetoothDeviceItem, newItem: BluetoothDeviceItem): Boolean {
            return  oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceItem, newItem: BluetoothDeviceItem): Boolean {
            return  oldItem.address == newItem.address
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = differ.currentList[position]

        holder.apply {
            name.text = item.name ?: "-no name-"
            address.text = item.address
        }
    }

    override fun getItemCount() = differ.currentList.size

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.name)
        val address: TextView = itemView.findViewById(R.id.address)
    }
}