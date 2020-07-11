package com.interpause.webcamdroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import java.lang.Exception
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.rtsp.RtspCamera2
import com.pedro.rtsp.rtsp.Protocol
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import net.ossrs.rtmp.ConnectCheckerRtmp

/*
camera.setProtocol(Protocol.UDP)
camera.setVideoCodec(VideoCodec.H265)
 */

class ControlsFragment : Fragment(), ConnectCheckerRtsp, SurfaceHolder.Callback {
    private var statusText: TextView? = null
    private var bitrateText: TextView? = null
    private var surfaceView: SurfaceView? = null
    private var streamButton: Button? = null
    private var selectorButton: Button? = null
    private var camera: RtspCamera2? = null
    private val camArgs: ControlsFragmentArgs by navArgs()
    private var url: String = "rtsp://192.168.42.62:8080/live/helpme"

    companion object {
        private const val PERMISSIONS_CALLBACK_ID = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
    }

    override fun onAuthErrorRtsp() {
        requireActivity().runOnUiThread{
            statusText?.text = getString(R.string.rtp_auth_error)
        }
        resetStream()
    }

    override fun onConnectionSuccessRtsp() {
        requireActivity().runOnUiThread {
            statusText?.text = getString(R.string.cam_addr_info, url)
            streamButton?.text = getString(R.string.btn_stop)
            streamButton?.isEnabled = true
        }
    }

    override fun onDisconnectRtsp() {
        requireActivity().runOnUiThread {
            statusText?.text = getString(R.string.cam_addr_init)
        }
        resetStream()
    }

    override fun onAuthSuccessRtsp() {

    }

    override fun onConnectionFailedRtsp(reason: String?) {
        requireActivity().runOnUiThread {
            statusText?.text = getString(R.string.rtp_connect_error,reason)
        }
        resetStream()
    }


    override fun onNewBitrateRtsp(bitrate: Long) {
        requireActivity().runOnUiThread {
            bitrateText?.text = getString(R.string.bitrate_info, bitrate)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        camera?.startPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        endStream()
        camera?.stopPreview()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_controls, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusText = view.findViewById<TextView>(R.id.statusText)
        bitrateText = view.findViewById<TextView>(R.id.bitrateText)
        surfaceView = view.findViewById<SurfaceView>(R.id.camPreview)
        streamButton = view.findViewById<Button>(R.id.streamButton)
        selectorButton = view.findViewById<Button>(R.id.selectorButton)
        statusText?.text = getString(R.string.cam_addr_init)
        //addrInfo?.text = getString(R.string.cam_addr_info, "lol sub", 1234)

        selectorButton?.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_controlsFragment_to_selectorFragment)
        }

        streamButton?.setOnClickListener {
            streamButton?.isEnabled = false
            if (!camera!!.isStreaming) {
                if(camera!!.isRecording || camera!!.prepareVideo(1920,1080,60,25000000,false,0) && camera!!.prepareAudio()) camera?.startStream(url)
                else{
                    endStream()
                    statusText?.text = getString(R.string.encoder_error)
                }
            }
            else endStream()
        }
        startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode != PERMISSIONS_CALLBACK_ID) return
        if(!permissionsGranted()){
            Handler().postDelayed({requireActivity().finish()}, 5000)
            return
        }
        startCamera()
    }

    private fun permissionsGranted() = REQUIRED_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(requireContext(),it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        if(!permissionsGranted()) {
            Toast.makeText(requireContext(), R.string.request_cam_permissions, Toast.LENGTH_LONG)
                .show()
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSIONS_CALLBACK_ID)
            return
        }
        camera = RtspCamera2(surfaceView!!,this)
        camera?.setProtocol(Protocol.UDP)
        camera?.disableAudio()
        selectorButton?.text = camArgs.name
        surfaceView?.holder?.addCallback(this)
    }

    private fun endStream(){
        if(!camera!!.isStreaming) return
        camera?.stopStream()
        resetStream()
    }

    private fun resetStream(){
        requireActivity().runOnUiThread {
            streamButton?.text = getString(R.string.btn_start)
            streamButton?.isEnabled = true
        }
    }
}