package com.example.happyplaces.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import com.example.happyplaces.R
import com.example.happyplaces.databinding.CustomLoadingButtonBinding

class CustomLoadingButton @JvmOverloads constructor(context: Context, private val attributeSet: AttributeSet,defAttrStyle: Int=0) :
    RelativeLayout(context, attributeSet,defAttrStyle) {
    companion object{
        val TAG:String = CustomLoadingButton::class.java.simpleName
    }
    private var binding: CustomLoadingButtonBinding
    private var attributes: TypedArray
    private var isLoading:Boolean = false

    init {
        val view = inflate(getContext(),R.layout.custom_loading_button,this)
        attributes = getContext()!!.obtainStyledAttributes(attributeSet,R.styleable.CustomLoadingButton)
        binding = CustomLoadingButtonBinding.bind(view)
        initViewWithStyle()
    }

    private fun initViewWithStyle() {
        if(attributes.hasValue(R.styleable.CustomLoadingButton_ButtonText)){
            binding.text.text = attributes.getString(R.styleable.CustomLoadingButton_ButtonText)
        }
        if(attributes.hasValue(R.styleable.CustomLoadingButton_FontSize)) {
            binding.text.setTextSize(TypedValue.COMPLEX_UNIT_SP,attributes.getFloat(
                    R.styleable.CustomLoadingButton_FontSize,
                    0.0f
                ))
        }
        if(attributes.hasValue(R.styleable.CustomLoadingButton_TextColor)) {
            binding.text.setTextColor(
                attributes.getColor(
                    R.styleable.CustomLoadingButton_TextColor,
                    -1
                )
            )
        }
        if(attributes.hasValue(R.styleable.CustomLoadingButton_BackgroundColor)) {
            binding.buttonCard.setBackgroundColor(attributes.getColor(
                R.styleable.CustomLoadingButton_BackgroundColor,
                -1))
        }
        if(attributes.hasValue(R.styleable.CustomLoadingButton_LoaderColor)) {
            val progressbarDrawable = binding.loader.indeterminateDrawable?.mutate()
            progressbarDrawable?.colorFilter = PorterDuffColorFilter(
                attributes.getColor(
                    R.styleable.CustomLoadingButton_LoaderColor,
                    -1
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.loader.progressDrawable = progressbarDrawable
        }

        binding.loader.visibility = View.GONE
        binding.text.visibility = View.VISIBLE
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        if(isLoading == loading)return;
        isLoading = loading
        if(loading){
            binding.loader.visibility = View.VISIBLE
            binding.text.visibility = View.GONE
            binding.root.isClickable = false

        }else{
            binding.loader.visibility = View.GONE
            binding.text.visibility = View.VISIBLE
            binding.root.isClickable = true
        }
    }

    fun isLoading():Boolean{
        return isLoading
    }

}