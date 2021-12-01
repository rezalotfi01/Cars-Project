package com.reza.carsproject.utils.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Local Implementation of QuickPermission library that will be used in app
 */

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
object PermissionsUtil {

    fun getDeniedPermissions(permissions: Array<String>, grantResults: IntArray): Array<String> =
        permissions.filterIndexed { index, s ->
            grantResults[index] == PackageManager.PERMISSION_DENIED
        }.toTypedArray()

    fun getPermanentlyDeniedPermissions(fragment: Fragment, permissions: Array<String>, grantResults: IntArray): Array<String> =
        permissions.filterIndexed { index, s ->
            grantResults[index] == PackageManager.PERMISSION_DENIED && !fragment.shouldShowRequestPermissionRationale(s)
        }.toTypedArray()

    /**
     * Returns true if the Activity has access to all given permissions.
     * Always returns true on platforms below M.
     *
     * @see Activity.checkSelfPermission
     */
    fun hasSelfPermission(activity: Context?, permissions: Array<String>): Boolean {
        // Verify that all required permissions have been granted
        activity?.let {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }

        return true
    }

}

data class QuickPermissionsOptions(
    var handleRationale: Boolean = true,
    var rationaleMessage: String = "",
    var handlePermanentlyDenied: Boolean = true,
    var permanentlyDeniedMessage: String = "",
    var rationaleMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    var permanentDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    var permissionsDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null
)

data class QuickPermissionsRequest(
    private var target: PermissionCheckerFragment,
    var permissions: Array<String> = emptyArray(),
    var handleRationale: Boolean = true,
    var rationaleMessage: String = "",
    var handlePermanentlyDenied: Boolean = true,
    var permanentlyDeniedMessage: String = "",
    internal var rationaleMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    internal var permanentDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    internal var permissionsDeniedMethod: ((QuickPermissionsRequest) -> Unit)? = null,
    var deniedPermissions: Array<String> = emptyArray(),
    var permanentlyDeniedPermissions: Array<String> = emptyArray()
) {
    /**
     * Proceed with requesting permissions again with user request
     */
    fun proceed() = target.requestPermissionsFromUser()

    /**
     * Cancels the current permissions request flow
     */
    fun cancel() = target.clean()

    /**
     * In case of permissions permanently denied, request user to enable from app settings
     */
    fun openAppSettings() = target.openAppSettings()
}



/**
* This fragment holds the single permission request and holds it until the flow is completed
*/
class PermissionCheckerFragment : Fragment() {

    private var quickPermissionsRequest: QuickPermissionsRequest? = null

    interface QuickPermissionsCallback {
        fun shouldShowRequestPermissionsRationale(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsGranted(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsPermanentlyDenied(quickPermissionsRequest: QuickPermissionsRequest?)
        fun onPermissionsDenied(quickPermissionsRequest: QuickPermissionsRequest?)
    }

    companion object {
        private const val TAG = "QuickPermissionsKotlin"
        private const val PERMISSIONS_REQUEST_CODE = 199
        fun newInstance(): PermissionCheckerFragment = PermissionCheckerFragment()
    }

    private var mListener: QuickPermissionsCallback? = null

    fun setListener(listener: QuickPermissionsCallback) {
        mListener = listener
        Log.d(TAG, "onCreate: listeners set")
    }

    private fun removeListener() {
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: permission fragment created")
    }

    fun setRequestPermissionsRequest(quickPermissionsRequest: QuickPermissionsRequest?) {
        this.quickPermissionsRequest = quickPermissionsRequest
    }

    private fun removeRequestPermissionsRequest() {
        quickPermissionsRequest = null
    }

    fun clean() {
        if (quickPermissionsRequest != null) {
            // permission request flow is finishing
            // let the caller receive callback about it
            if (quickPermissionsRequest?.deniedPermissions?.size ?: 0 > 0)
                mListener?.onPermissionsDenied(quickPermissionsRequest)

            removeRequestPermissionsRequest()
            removeListener()
        } else {
            Log.w(
                TAG, "clean: QuickPermissionsRequest has already completed its flow. " +
                    "No further callbacks will be called for the current flow.")
        }
    }

    fun requestPermissionsFromUser() {
        if (quickPermissionsRequest != null) {
            Log.d(TAG, "requestPermissionsFromUser: requesting permissions")
            requestPermissions(quickPermissionsRequest?.permissions.orEmpty(), PERMISSIONS_REQUEST_CODE)
        } else {
            Log.w(
                TAG, "requestPermissionsFromUser: QuickPermissionsRequest has already completed its flow. " +
                    "Cannot request permissions again from the request received from the callback. " +
                    "You can start the new flow by calling runWithPermissions() { } again.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "passing callback")

        // check if permissions granted
        handlePermissionResult(permissions, grantResults)
    }

    /**
     * Checks and takes the action based on permission results retrieved from onRequestPermissionsResult
     * and from the settings activity
     *
     * @param permissions List of Permissions
     * @param grantResults A list of permission result <b>Granted</b> or <b>Denied</b>
     */
    private fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        // add a check with the permissions list
        // if the permissions list is empty, that means system has told that permissions request
        // is invalid somehow or discarded the previous request
        // this can happen in case when the multiple permissions requests are sent
        // simultaneously to the system
        if (permissions.isEmpty()) {
            Log.w(TAG, "handlePermissionResult: Permissions result discarded. You might have called multiple permissions request simultaneously")
            return
        }

        if (PermissionsUtil.hasSelfPermission(context, permissions)) {

            // set the denied permissions to empty as all the permissions are granted
            // this is required as clean will be called which can invoke on permissions denied
            // if it finds some permissions in the denied list
            quickPermissionsRequest?.deniedPermissions = emptyArray()

            // we are good to go!
            mListener?.onPermissionsGranted(quickPermissionsRequest)

            // flow complete
            clean()
        } else {
            // we are still missing permissions
            val deniedPermissions = PermissionsUtil.getDeniedPermissions(permissions, grantResults)
            quickPermissionsRequest?.deniedPermissions = deniedPermissions

            // check if rationale dialog should be shown or not
            var shouldShowRationale = true
            var isPermanentlyDenied = false
            for (i in 0 until deniedPermissions.size) {
                val deniedPermission = deniedPermissions[i]
                val rationale = shouldShowRequestPermissionRationale(deniedPermission)
                if (!rationale) {
                    shouldShowRationale = false
                    isPermanentlyDenied = true
                    break
                }
            }

            if (quickPermissionsRequest?.handlePermanentlyDenied == true && isPermanentlyDenied) {

                quickPermissionsRequest?.permanentDeniedMethod?.let {
                    // get list of permanently denied methods
                    quickPermissionsRequest?.permanentlyDeniedPermissions =
                        PermissionsUtil.getPermanentlyDeniedPermissions(
                            this,
                            permissions,
                            grantResults
                        )
                    mListener?.onPermissionsPermanentlyDenied(quickPermissionsRequest)
                    return
                }


                MaterialAlertDialogBuilder(requireContext()).setMessage(quickPermissionsRequest?.permanentlyDeniedMessage.orEmpty())
                    .setPositiveButton("Setting"){_,_ ->
                        openAppSettings()
                    }.setNegativeButton("Cancel"){ _,_ ->
                        clean()
                    }.setCancelable(false)
                    .show()
                return
            }

            // if should show rationale dialog
            if (quickPermissionsRequest?.handleRationale == true && shouldShowRationale) {

                quickPermissionsRequest?.rationaleMethod?.let {
                    mListener?.shouldShowRequestPermissionsRationale(quickPermissionsRequest)
                    return
                }

                MaterialAlertDialogBuilder(requireContext()).setMessage(quickPermissionsRequest?.permanentlyDeniedMessage.orEmpty())
                    .setPositiveButton("Setting"){_,_ ->
                        openAppSettings()
                    }.setNegativeButton("Cancel"){ _,_ ->
                        clean()
                    }.setCancelable(false)
                    .show()

                return
            }

            // if handlePermanentlyDenied = false and handleRationale = false
            // This will call permissionsDenied method
            clean()
        }
    }

    fun openAppSettings() {
        if (quickPermissionsRequest != null) {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS,
                fromParts("package", activity?.packageName, null))
            //                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE)
        } else {
            Log.w(TAG, "openAppSettings: QuickPermissionsRequest has already completed its flow. Cannot open app settings")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val permissions = quickPermissionsRequest?.permissions ?: emptyArray()
            val grantResults = IntArray(permissions.size)
            permissions.forEachIndexed { index, s ->
                grantResults[index] = context?.let { ActivityCompat.checkSelfPermission(it, s) } ?: PackageManager.PERMISSION_DENIED
            }

            handlePermissionResult(permissions, grantResults)
        }
    }
}