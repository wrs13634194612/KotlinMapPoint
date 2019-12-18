package com.ufi.pdioms.ztkotlin

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.InfoWindowAdapter
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.maps.utils.SpatialRelationUtil
import com.amap.api.maps.utils.overlay.MovingPointOverlay
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var mMapView: MapView? = null
    var mAMap: AMap? = null
    var mPolyline: Polyline? = null
    var smoothMarker:MovingPointOverlay?=null
    var marker: Marker?=null
    var list: MutableList<LatLng>? = null
    internal var infoWindowLayout: LinearLayout? = null
    internal var title: TextView?=null
    internal var snippet: TextView?=null

    /**
     * 个性化定制的信息窗口视图的类
     * 如果要定制化渲染这个信息窗口，需要重载getInfoWindow(Marker)方法。
     * 如果只是需要替换信息窗口的内容，则需要重载getInfoContents(Marker)方法。
     */
    internal var infoWindowAdapter: AMap.InfoWindowAdapter = object : AMap.InfoWindowAdapter {

        // 个性化Marker的InfoWindow 视图
        // 如果这个方法返回null，则将会使用默认的信息窗口风格，内容将会调用getInfoContents(Marker)方法获取
        override fun getInfoWindow(marker: Marker): View {

            return getInfoWindowView(marker)
        }

        // 这个方法只有在getInfoWindow(Marker)返回null 时才会被调用
        // 定制化的view 做这个信息窗口的内容，如果返回null 将以默认内容渲染
        override fun getInfoContents(marker: Marker): View {

            return getInfoWindowView(marker)
        }
    }

    /**
     * 自定义View并且绑定数据方法
     *
     * @param marker 点击的Marker对象
     * @return 返回自定义窗口的视图
     */
    private fun getInfoWindowView(marker: Marker): View {
        if (infoWindowLayout == null) {
            infoWindowLayout = LinearLayout(this)
            infoWindowLayout!!.setOrientation(LinearLayout.VERTICAL)
            title = TextView(this)
            snippet = TextView(this)
            title!!.setText("距离距离展示")
            title!!.setTextColor(Color.BLACK)
            snippet!!.setTextColor(Color.BLACK)
            infoWindowLayout!!.setBackgroundResource(R.mipmap.custom_info_bubble)

            infoWindowLayout!!.addView(title)
            infoWindowLayout!!.addView(snippet)
        }

        return infoWindowLayout as LinearLayout
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMapView = findViewById(R.id.map) as MapView
        mMapView!!.onCreate(savedInstanceState)

        if(mAMap == null){
            mAMap = mMapView!!.map
        }

        list = ArrayList()
        list!!.add(LatLng(23.159054, 113.401049))
        list!!.add(LatLng(23.16225, 113.401393))
        list!!.add(LatLng(23.165801, 113.400062))
        list!!.add(LatLng(23.167221, 113.399161))
        list!!.add(LatLng(23.167261, 113.394311))

        button_set.setOnClickListener { addPolylineInPlayGround() }
        button_start.setOnClickListener { addMove() }
    }


    private fun addPolylineInPlayGround(){
        mPolyline = mAMap!!.addPolyline(
            PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture)) //setCustomTextureList(bitmapDescriptors)
                //				.setCustomTextureIndex(texIndexList)
                .addAll(list)
                .useGradient(true)
                .width(18f)
        )

        val builder = LatLngBounds.Builder()
        builder.include(list!!.get(0))
        builder.include(list!!.get(list!!.size - 2))
        mAMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
    }

    private fun addMove(){
        if (mPolyline == null) {
            return
        }
        // 构建 轨迹的显示区域
        val builder = LatLngBounds.Builder()
        builder.include(list!!.get(0))
        builder.include(list!!.get(list!!.size - 2))
        mAMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50))
        // 实例 MovingPointOverlay 对象
        if (smoothMarker == null) {
            // 设置 平滑移动的 图标
            marker = mAMap!!.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car)).anchor(
                    0.5f,
                    0.5f
                )
            )
            smoothMarker = MovingPointOverlay(mAMap, marker)
        }

        // 取轨迹点的第一个点 作为 平滑移动的启动
        val drivePoint = list!!.get(0)
        val pair = SpatialRelationUtil.calShortestDistancePoint(list, drivePoint)
        list!!.set(pair.first, drivePoint)
        val subList = list!!.subList(pair.first, list!!.size)

        // 设置轨迹点
        smoothMarker!!.setPoints(subList)
        // 设置平滑移动的总时间  单位  秒
        smoothMarker!!.setTotalDuration(40)

        // 设置  自定义的InfoWindow 适配器
        mAMap!!.setInfoWindowAdapter(infoWindowAdapter)
        // 显示 infowindow
        marker!!.showInfoWindow()
        // 设置移动的监听事件  返回 距终点的距离  单位 米
        // smoothMarker.setMoveListene
        // 开始移动
        smoothMarker!!.startSmoothMove()
    }



    override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView!!.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (smoothMarker!=null){
            smoothMarker!!.setMoveListener(null)
            smoothMarker!!.destroy()
        }
        mMapView!!.onDestroy()
    }





}
