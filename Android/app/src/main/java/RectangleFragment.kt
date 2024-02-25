import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.obsfreegdsc.obsfree.MapsActivity
import com.obsfreegdsc.obsfree.R
import com.obsfreegdsc.obsfree.databinding.FragmentRectangleBinding

class RectangleFragment : Fragment() {
    private var _viewBinding: FragmentRectangleBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = FragmentRectangleBinding.inflate(inflater, container, false)

        viewBinding.mapButtonWhole.setOnClickListener { intentMap() }

        return viewBinding.root
    }

    private fun intentMap() {
        val intent = Intent(activity, MapsActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}


//class RectangleFragment : Fragment() {
//    private lateinit var viewBinding: FragmentRectangleBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//
//    ): View? {
//        // Inflate the layout for this fragment
//        viewBinding.mapButtonWhole.setOnClickListener { intentMap() }
//        return inflater.inflate(R.layout.fragment_rectangle, container, false)
//
//    }
//
//    private fun intentMap(){
//        val intent = Intent(activity, MapsActivity::class.java)
//        startActivity(intent)
//    }
//}