package com.teckudos.demoapplication.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teckudos.demoapplication.R

class PermissionUtils(
    var context: Context,
    val isFromActivity: Boolean,
    var requestCode: Int,
    val requiredPermissions: ArrayList<String>,
    val permissionListener: PermissionListener
) {
    fun requestPermission() = if (!hasPermissions(context, requiredPermissions)) {
        request()
    } else {
        permissionListener.onPermissionsGranted(requestCode, requiredPermissions)
    }

    private fun hasPermissions(context: Context?, permissions: ArrayList<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun request() {
        if (isFromActivity) {
            ActivityCompat.requestPermissions(
                context as Activity, requiredPermissions.toArray(
                    arrayOfNulls(requiredPermissions.size)
                ), requestCode
            )
        } else {
            (context as FragmentActivity).requestPermissions(
                requiredPermissions.toArray(
                    arrayOfNulls(requiredPermissions.size)
                ), requestCode
            )
        }
    }

    fun onRequestPermissionsResult(
        reqCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when {
            requestCode == reqCode -> {
                val grantedPermissionList = ArrayList<String>()
                val deniedPermissionList = ArrayList<String>()
                val neverAskPermissionList = ArrayList<String>()
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissionList.add(permissions[i])
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                permissions[i]
                            )
                        ) deniedPermissionList.add(permissions[i])
                        else neverAskPermissionList.add(permissions[i])
                    }
                }

                if (grantedPermissionList.isNotEmpty()) permissionListener.onPermissionsGranted(
                    requestCode,
                    grantedPermissionList
                )
                if (deniedPermissionList.isNotEmpty()) permissionListener.onPermissionsDenied(
                    requestCode,
                    deniedPermissionList
                )
                if (neverAskPermissionList.isNotEmpty()) permissionListener.onPermissionDeniedWithNeverAsk(
                    requestCode,
                    neverAskPermissionList
                )
            }
        }
    }

    data class Builder(
        private var context: Context? = null,
        private var fromActivity: Boolean? = null,
        private var requestCode: Int? = null,
        private var askFor: ArrayList<Permission>? = null,
        private var rationalMessage: String? = null,
        private var permissionListener: PermissionListener? = null
    ) {

        var requiredPermissions = arrayListOf<String>()

        fun with(context: Context) = apply { this.context = context }

        fun isFromActivity(fromActivity: Boolean) = apply { this.fromActivity = fromActivity }

        fun requestCode(requestCode: Int) = apply { this.requestCode = requestCode }

        fun askFor(vararg permissions: Permission) = apply {
            for (permission in permissions) when (permission) {
                Permission.CAMERA -> requiredPermissions.add(Manifest.permission.CAMERA)
                Permission.STORAGE -> {
                    requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        fun permissionListener(permissionListener: PermissionListener?) =
            apply { this.permissionListener = permissionListener }

        private fun <T> notNull(vararg elements: T): Boolean {
            elements.forEach {
                if (it == null) {
                    return false
                }
            }
            return true
        }

        fun build() =
            if (notNull(
                    context,
                    fromActivity,
                    requestCode,
                    requiredPermissions,
                    permissionListener
                ) && requiredPermissions.isNotEmpty()
            ) {
                PermissionUtils(
                    context!!,
                    fromActivity!!,
                    requestCode!!,
                    requiredPermissions,
                    permissionListener!!
                )
            } else {
                null
            }
    }
}

interface PermissionListener {
    fun onPermissionsGranted(requestCode: Int, grantedPermissionList: ArrayList<String>)
    fun onPermissionsDenied(requestCode: Int, deniedPermissionList: ArrayList<String>)
    fun onPermissionDeniedWithNeverAsk(
        requestCode: Int,
        deniedPermissionsWithNeverAsk: ArrayList<String>
    )
}

class PermissionDialogFragment(
    private val mContext: Context,
    private val message: String,
    private val positiveBtnListener: DialogInterface.OnClickListener,
    private val negativeBtnListener: DialogInterface.OnClickListener,
    private val positiveBtnName: String = mContext.getString(R.string.ok),
    private val negativeBtnName: String = mContext.getString(R.string.cancel)
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(mContext)
            .setMessage(message)
            .setPositiveButton(positiveBtnName, positiveBtnListener)
            .setNegativeButton(negativeBtnName, negativeBtnListener)
            .create()
    }
}

enum class Permission {
    CAMERA, STORAGE
}

/*
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 101
        private const val SETTING_REQUEST_CODE = 102
    }

    private lateinit var binding: ActivitySplashBinding
    private var permissionUtils: PermissionUtils? = null
    private var grantedPermissions = arrayListOf<String>()
    private var deniedPermissions = arrayListOf<String>()
    private var neverAskPermissions = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        supportActionBar?.hide()
        checkPermission()
    }

    private fun checkPermission() {
        permissionUtils = PermissionUtils.Builder()
            .with(this)
            .isFromActivity(true)
            .requestCode(REQUEST_CODE)
            .askFor(Permission.CAMERA, Permission.STORAGE)
            .permissionListener(object : PermissionListener {
                override fun onPermissionsGranted(
                    requestCode: Int,
                    grantedPermissionList: ArrayList<String>
                ) {
                    grantedPermissions.clear()
                    grantedPermissions.addAll(grantedPermissionList)
                    if (grantedPermissions.size == 3) {
                        setDestination()
                    }
                }

                override fun onPermissionsDenied(
                    requestCode: Int,
                    deniedPermissionList: ArrayList<String>
                ) {
                    deniedPermissions.clear()
                    deniedPermissions.addAll(deniedPermissionList)
                    if (deniedPermissions.size > 0)
                        PermissionDialogFragment(this@SplashActivity,
                            resources.getString(
                                R.string.rational_message,
                                Permission.CAMERA,
                                Permission.STORAGE
                            ),
                            DialogInterface.OnClickListener { _, _ -> checkPermission() },
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }).show(supportFragmentManager, "")
                    else {
                        setDestination()
                    }
                }

                override fun onPermissionDeniedWithNeverAsk(
                    requestCode: Int,
                    deniedPermissionsWithNeverAsk: ArrayList<String>
                ) {
                    neverAskPermissions.clear()
                    neverAskPermissions.addAll(deniedPermissionsWithNeverAsk)
                    if (neverAskPermissions.size > 0) {
                        PermissionDialogFragment(this@SplashActivity,
                            resources.getString(
                                R.string.permission_denied_message
                            ),
                            DialogInterface.OnClickListener { _, _ -> goToSettings() },
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }).show(supportFragmentManager, "")
                    }
                }

            }).build()
        permissionUtils?.requestPermission()
    }

    private fun setDestination() {
        runBlocking {
            delay(2000)
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts(getString(R.string.package_string), packageName, null)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivityForResult(intent, SETTING_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SETTING_REQUEST_CODE) {
            checkPermission()
            if (neverAskPermissions.size > 0)
                finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
*/
