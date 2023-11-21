package com.deyi.daxie.cloud.service.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.deyi.daxie.cloud.service.bean.*;
import com.deyi.daxie.cloud.service.mapper.*;
import com.deyi.daxie.cloud.service.util.Constant;
import com.deyi.daxie.cloud.service.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
@Slf4j
@Service
public class LessonMsgServiceImpl implements LessonMsgService{
    @Autowired
    private  VelStatusDataMapper velStatusDataMapper;
    @Autowired
    private  VelControlDataMapper velControlDataMapper;
    @Autowired
    private  VelMissionDataMapper velMissionDataMapper;
    @Autowired
    private  VelWarnDataMapper velWarnDataMapper;
    @Autowired
    private  VelAligningDataMapper velAligningDataMapper;
    @Autowired
    private  VelObstacleDataMapper velObstacleDataMapper;
    @Autowired
    private  TcsLoginMapper tcsLoginMapper;
    @Autowired
    private  TcsLogoutMapper tcsLogoutMapper;
    @Autowired
    private  TcsUploadTruckStatusMapper tcsUploadTruckStatusMapper;
    @Autowired
    private  TcsUploadAliveMapper tcsUploadAliveMapper;
    @Autowired
    private  TcsGetLedMessageMapper tcsGetLedMessageMapper;
    @Autowired
    private  TcsGetInstructionsMapper tcsGetInstructionsMapper;
    @Autowired
    private  TcsFocusChannelMapper tcsFocusChannelMapper;
    @Autowired
    private  TcsGetYqInfoMapper tcsGetYqInfoMapper;
    @Autowired
    private  TcsUploadTruckNoToLiHuoMapper tcsUploadTruckNoToLiHuoMapper;
    @Autowired
    private  TcsQcPositionMapper tcsQcPositionMapper;
    @Autowired
    private  TcsGetQdGpsAllMapper tcsGetQdGpsAllMapper;
    @Autowired
    private  TcsGetQdLaneInfoMapper tcsGetQdLaneInfoMapper;
    @Autowired
    private  TcsSendNoticeMapper tcsSendNoticeMapper;
    @Autowired
    private  TcsTrucktotcsConfirmqccpsendMapper tcsTrucktotcsConfirmqccpsendMapper;
    @Autowired
    private  TcsRtgTruckMapper tcsRtgTruckMapper;
    @Autowired
    private  TcsRtgChangeRoadMapper tcsRtgChangeRoadMapper;
    @Autowired
    private  TcsRtgPositionMapper tcsRtgPositionMapper;
    @Autowired
    private  TcsRtgConfirmcpsstopMapper tcsRtgConfirmcpsstopMapper;
    @Autowired
    private  TcsGetLmdGpsAllMapper tcsGetLmdGpsAllMapper;
    @Autowired
    private  TcsGetLjGpsAllMapper tcsGetLjGpsAllMapper;
    @Autowired
    private  TcsWebsocketGetLmdDwMapper tcsWebsocketGetLmdDwMapper;
    @Autowired
    private  TcsWebsocketGetQdGpsMapper tcsWebsocketGetQdGpsMapper;
    @Autowired
    private  TcsWebsocketGetQdInfoMapper tcsWebsocketGetQdInfoMapper;
    @Autowired
    private  TcsWebsocketGetQdDwMapper tcsWebsocketGetQdDwMapper;
    @Autowired
    private  TcsWebsocketGetlmdInfoMapper tcsWebsocketGetlmdInfoMapper;
    @Autowired
    private  TcsWebsocketGetQdMvMapper tcsWebsocketGetQdMvMapper;
    @Autowired
    private  TcsWebsocketTruckDriveAwayMapper tcsWebsocketTruckDriveAwayMapper;
    @Autowired
    private  TcsWebsocketLockDriveAwayMapper tcsWebsocketLockDriveAwayMapper;
    @Autowired
    private  TcsApiLockarriveMapper tcsApiLockarriveMapper;

    @Override
    public void save(String message) {
        try {
            JSONObject obj = JSON.parseObject(message);
            log.info("+++++message{}",message);
            MessageType type = MessageType.getByValue(obj.getString("messageType"));
            if (type == null) {
                  new Result(Constant.HTTP_ERROR, "Invalid request, data type does not exist");
            }
            switch (Objects.requireNonNull(type)) {
                /*case LOGIN:
                     login(obj);
                case LOGIN_OUT:
                     logout(obj);*/
                case ALIVE:
                     alive(obj);
                case SET_TRUCK_STATUS:
                     uploadTruckStatus(obj);
                case GET_LED_MSG:
                     getLedMessage(obj);
                case GET_LAST_ORDER:
                     getInstructions(obj);
                case QC_INFO:
                    getChangedType(obj);
                case QD_CPS_INFO:
                     qdCpsInfo(obj);
                case GET_QC_INFO:
                case  YARD_MOVE:
                     getYardMove(obj);
                case TOS_ORDER:
                     tosOrder(obj);
                case QD_BAYS_CPSINFO:
                     getQdBaysCpsInfo(obj);
                case LMD_CPSINFO:
                     getLmdCpsInfo(obj);
                case QC_MOVE_STATUS:
                     getQcMoveStatus(obj);
                case UN_LOCK:
                     getUnLock(obj);
                case RTG_MOVE_STATUS:
                     focusChannel(obj);
                case GET_YQ:
                     getYqInfo(obj);
                case LI_HUO:
                     uploadTruckLiHuo(obj);
                case TCS_QC_POSITION:
                     tcsQcPosition(obj);
                case GET_LANE:
                     getQdLaneInfo(obj);
                case GET_GPS:
                     getQdGpsAll(obj);
                case SEND_PROMPT:
                     sendNotice(obj);
                case CONFIRM_QC_CPS_END:
                     confirmQcCpsEnd(obj);
                case TCS_RTG_TRUCK:
                     tcsRtgTruck(obj);
                case GET_CHANGE_ROAD:
                     changeRoad(obj);
                case TCS_RTG_POSITION:
                     uploadPosition(obj);
                case RTG_INPLACE:
                     confirmCpsStop(obj);
                case GET_LONG_MEN_DIAO_GPS:
                     getLmdGpsAll(obj);
                case GET_DUI_GAO_JI_GPS:
                     getLjGpsAll(obj);

                default:
                     new Result(Constant.HTTP_INVALID, "Invalid request");
            }
        } catch (Exception e) {
             new Result(Constant.HTTP_ERROR, "Invalid request, " + e.getMessage());
        }
    }

    /**
     * 通用应发to车辆
     *
     * @param messageType messageType
     * @param response    response
     * @return Result
     */
    private Result commonToVel(String messageType, JSONObject response) {
        // TCS应答失败
        if (!Objects.equals("200", Constant.TCS_SUCCESS)) {
            return Result.error(messageType, response);
        }
        return Result.success(messageType, response);
    }

    /**
     * 3.1.3.上传心跳
     *
     * @param obj obj
     * @return Result
     */
    public void alive(JSONObject obj) {
        TcsUploadAlive tcsUploadAlive = new TcsUploadAlive();
        tcsUploadAlive.setTime(new Date());
        tcsUploadAlive.setToken(obj.getString("truckNo"));
        tcsUploadAlive.setParamPassword(obj.getString("password"));
        tcsUploadAlive.setResCode(obj.get("code").toString());
        tcsUploadAlive.setTruckNo(obj.getString("truckNo"));
        tcsUploadAlive.setResMessage(obj.getString("messageType"));
        tcsUploadAlive.setResData(obj.get("data").toString());
        tcsUploadAlive.setParamUsername(obj.getString("username"));
        tcsUploadAliveMapper.add(tcsUploadAlive);
    }

    /**
     * 3.1.4.上传集卡状态-TCS
     *
     * @param obj obj
     * @return Result
     */
    public void uploadTruckStatus(JSONObject obj) {
        //JSONObject response = tcsHttpApi.uploadTruckStatus(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("上传集卡状态----obj{}", obj);
        TcsUploadTruckStatus tcsUploadTruckStatus = new TcsUploadTruckStatus();
        tcsUploadTruckStatus.setTruckNo(obj.getString("truckNo"));
        tcsUploadTruckStatus.setTime(new Date());
        tcsUploadTruckStatus.setToken(obj.getString("truckNo"));
        tcsUploadTruckStatus.setResData(obj.getString("data"));
        tcsUploadTruckStatus.setResCode("200");
        tcsUploadTruckStatus.setParamLat(obj.getString("lat"));
        tcsUploadTruckStatus.setResMessage(obj.getString("messageType"));
        tcsUploadTruckStatus.setParamLng(obj.getString("lng"));
        tcsUploadTruckStatus.setParamSpeed(obj.getString("speed"));
        tcsUploadTruckStatus.setParamTime(obj.getString("time"));
        tcsUploadTruckStatus.setParamTruckno(obj.getString("truckNo"));
        log.info("上传集卡状态----tcsUploadTruckStatus{}", tcsUploadTruckStatus);
        tcsUploadTruckStatusMapper.add(tcsUploadTruckStatus);
    }

    /**
     * 3.1.5.获取LED显示的内容
     *
     * @param obj obj
     * @return Result
     */
    public void getLedMessage(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.getLedMessage(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("获取LED显示的内容truckNo{}", obj);
        TcsGetLedMessage tcsGetLedMessage = new TcsGetLedMessage();
        tcsGetLedMessage.setResMessage(obj.getString("messageType"));
        tcsGetLedMessage.setTime(new Date());
        tcsGetLedMessage.setParamTruckno(obj.getString("truckNo"));
        tcsGetLedMessage.setResCode(obj.getString("code"));
        tcsGetLedMessage.setToken(obj.getString("truckNo"));
        tcsGetLedMessage.setResData(obj.getString("message"));
        tcsGetLedMessage.setTruckNo(obj.getString("truckNo"));
        tcsGetLedMessageMapper.add(tcsGetLedMessage);
    }

    /**
     * 3.1.6.查询最新的指令信息
     *
     * @param obj obj
     * @return Result
     */
    public void getInstructions(JSONObject obj) {
        //JSONObject response = tcsHttpApi.getInstructions(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("查询最新的指令信息truckNo{}", obj);
        TcsGetInstructions tcsGetInstructions = new TcsGetInstructions();
        tcsGetInstructions.setResMessage(obj.getString("messageType"));
        tcsGetInstructions.setTime(new Date());
        tcsGetInstructions.setParamTruckno(obj.getString("truckNo"));
        tcsGetInstructions.setResCode(obj.getString("code"));
        tcsGetInstructions.setToken(obj.getString("truckNo"));
        tcsGetInstructions.setResData(obj.getString("data"));
        tcsGetInstructions.setTruckNo(obj.getString("truckNo"));
        tcsGetInstructionsMapper.add(tcsGetInstructions);
    }

    /**
     * 3.1.7.业务信息订阅
     *
     * @param obj obj
     * @return Result
     */
    public void focusChannel(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.focusChannel(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("业务信息订阅truckNo{},", obj);
        TcsFocusChannel tcsFocusChannel = new TcsFocusChannel();
        tcsFocusChannel.setResMessage(obj.getString("messageType"));
        tcsFocusChannel.setTime(new Date());
        tcsFocusChannel.setParamChannelname(obj.getString("channelName"));
        tcsFocusChannel.setParamTruck(obj.getString("truck"));
        tcsFocusChannel.setParamIsfocus(obj.getBooleanValue("isFocus"));
        tcsFocusChannel.setResCode(obj.getString("code"));
        tcsFocusChannel.setToken(obj.getString("truckNo"));
        tcsFocusChannel.setResData(obj.getString("data"));
        tcsFocusChannel.setTruckNo(obj.getString("truck"));
        tcsFocusChannelMapper.add(tcsFocusChannel);
    }

    /**
     * 3.2.1.获取上下引桥数据
     *
     * @param obj obj
     * @return Result
     */
    public void getYqInfo(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.getYqInfo(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("获取上下引桥数据truckNo{}", obj);
        TcsGetYqInfo tcsGetYqInfo = new TcsGetYqInfo();
        tcsGetYqInfo.setResMessage(obj.getString("messageType"));
        tcsGetYqInfo.setTime(new Date());
        tcsGetYqInfo.setParamQc(obj.getString("qc"));
        tcsGetYqInfo.setResCode(obj.getString("code"));
        tcsGetYqInfo.setToken(obj.getString("truckNo"));
        tcsGetYqInfo.setResData(obj.getString("data"));
        tcsGetYqInfo.setTruckNo(obj.getString("truckNo"));
        tcsGetYqInfoMapper.add(tcsGetYqInfo);
    }

    /**
     * 3.2.2.理货上报车号功能
     *
     * @param obj obj
     * @return Result
     */
    public void uploadTruckLiHuo(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.uploadTruckLiHuo(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("理货上报车号功能truckNo{}", obj);
        TcsUploadTruckNoToLiHuo tcsUploadTruckNoToLiHuo = new TcsUploadTruckNoToLiHuo();
        tcsUploadTruckNoToLiHuo.setTime(new Date());
        tcsUploadTruckNoToLiHuo.setTruckNo(obj.getString("truckNo"));
        tcsUploadTruckNoToLiHuo.setToken(obj.getString("truckNo"));
        tcsUploadTruckNoToLiHuo.setParamMessageinfo(obj.getString("MessageInfo"));
        tcsUploadTruckNoToLiHuo.setParamQcInfo(obj.getString("QC_Info"));
        tcsUploadTruckNoToLiHuo.setParamTruckInfo(obj.getString("Truck_Info"));
        tcsUploadTruckNoToLiHuo.setResCode(obj.getString("code"));
        tcsUploadTruckNoToLiHuo.setResData(obj.getString("data"));
        tcsUploadTruckNoToLiHuo.setResMessage(obj.getString("messageType"));
        tcsUploadTruckNoToLiHuoMapper.add(tcsUploadTruckNoToLiHuo);
    }

    /**
     * 3.2.3.桥吊下集卡到位
     *
     * @param obj obj
     * @return Result
     */
    public void tcsQcPosition(JSONObject obj) {
       // //JSONObject response = tcsHttpApi.tcsQcPosition(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("桥吊下集卡到位truckNo{}", obj);
        TcsQcPosition TcsQcPosition = new TcsQcPosition();
        TcsQcPosition.setTime(new Date());
        TcsQcPosition.setTruckNo(obj.getString("truckNo"));
        TcsQcPosition.setToken(obj.getString("truckNo"));
        TcsQcPosition.setParamEpno(obj.getString("epno"));
        TcsQcPosition.setParamIsarrive(obj.getString("isarrive"));
        TcsQcPosition.setParamEptype(obj.getString("eptype"));
        TcsQcPosition.setParamArrivetime(obj.getString("arrivetime"));
        TcsQcPosition.setParamLane(obj.getString("lane"));
        TcsQcPosition.setParamLon(obj.getString("lon"));
        TcsQcPosition.setParamLat(obj.getString("lat"));
        TcsQcPosition.setResCode(obj.getString("code"));
        TcsQcPosition.setResData(obj.getString("data"));
        TcsQcPosition.setResMessage(obj.getString("messageType"));
        tcsQcPositionMapper.add(TcsQcPosition);
    }

    /**
     * 3.2.4.获取桥吊作业车道数据
     *
     * @param obj obj
     * @return Result
     */
    public void getQdLaneInfo(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.getQdLaneInfo(obj.getString("truckNo"));
        log.info("获取桥吊作业车道数据truckNo{}", obj);
        TcsGetQdLaneInfo tcsGetQdLaneInfo = new TcsGetQdLaneInfo();

        tcsGetQdLaneInfo.setTime(new Date());
        tcsGetQdLaneInfo.setTruckNo(obj.getString("truckNo"));
        tcsGetQdLaneInfo.setToken(obj.getString("truckNo"));
        tcsGetQdLaneInfo.setResCode(obj.getString("code"));
        tcsGetQdLaneInfo.setResMessage(obj.getString("messageType"));
        tcsGetQdLaneInfo.setResData(obj.getString("data"));
        log.info("data111222{}", obj.getString("data"));
        log.info("data2222{}", obj.get("data"));
        log.info("获取所有桥吊GPS数据tcsGetQdLaneInfo{}", tcsGetQdLaneInfo);
        tcsGetQdLaneInfoMapper.add(tcsGetQdLaneInfo);
    }

    /**
     * 3.2.5.获取所有桥吊GPS数据
     *
     * @param obj obj
     * @return Result
     */
    public void getQdGpsAll(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.getQdGpsAll(obj.getString("truckNo"));
        log.info("获取所有桥吊GPS数据truckNo{}", obj);
        TcsGetQdGpsAll tcsGetQdGpsAll = new TcsGetQdGpsAll();
        tcsGetQdGpsAll.setTime(new Date());
        tcsGetQdGpsAll.setTruckNo(obj.getString("truckNo"));
        tcsGetQdGpsAll.setToken(obj.getString("truckNo"));
        tcsGetQdGpsAll.setResCode(obj.getString("code"));
        tcsGetQdGpsAll.setResMessage(obj.getString("message"));
        tcsGetQdGpsAll.setResData(obj.getString("data"));
        log.info("data111222{}", obj.getString("data"));
        log.info("data2222{}", obj.get("data"));
        log.info("获取所有桥吊GPS数据tcsGetQdGpsAll{}", tcsGetQdGpsAll);
        tcsGetQdGpsAllMapper.add(tcsGetQdGpsAll);
    }

    /**
     * 3.2.6.发送提示信息至桥吊
     *
     * @param obj obj
     * @return Result
     */
    public void sendNotice(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.sendNotice(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("发送提示信息至桥吊truckNo{},response{}", obj);
        TcsSendNotice tcsSendNotice = new TcsSendNotice();
        tcsSendNotice.setTime(new Date());
        tcsSendNotice.setTruckNo(obj.getString("truckNo"));
        tcsSendNotice.setToken(obj.getString("truckNo"));
        tcsSendNotice.setParamTruckno(obj.getString("truckNo"));
        tcsSendNotice.setParamNotice(obj.getString("notice"));
        tcsSendNotice.setResCode(obj.getString("code"));
        tcsSendNotice.setResMessage(obj.getString("messageType"));
        tcsSendNotice.setResData(obj.getString("data"));
        tcsSendNoticeMapper.add(tcsSendNotice);
    }

    /**
     * 3.2.7.无人集卡桥吊下对位完成信息
     *
     * @param obj obj
     * @return Result
     */
    public void confirmQcCpsEnd(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.confirmQcCpsEnd(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("无人集卡桥吊下对位完成信息truckNo{}", obj);
        TcsTrucktotcsConfirmqccpsend tcsTrucktotcsConfirmqccpsend = new TcsTrucktotcsConfirmqccpsend();
        tcsTrucktotcsConfirmqccpsend.setTime(new Date());
        tcsTrucktotcsConfirmqccpsend.setTruckNo(obj.getString("truckNo"));
        tcsTrucktotcsConfirmqccpsend.setToken(obj.getString("truckNo"));
        tcsTrucktotcsConfirmqccpsend.setParamTruck(obj.getString("truck"));
        tcsTrucktotcsConfirmqccpsend.setParamQc(obj.getString("qc"));
        tcsTrucktotcsConfirmqccpsend.setParamTime(obj.getString("time"));
        tcsTrucktotcsConfirmqccpsend.setResCode(obj.get("code").toString());
        tcsTrucktotcsConfirmqccpsend.setResMessage(obj.get("messageType").toString());
        tcsTrucktotcsConfirmqccpsend.setResData(obj.get("data").toString());
        tcsTrucktotcsConfirmqccpsendMapper.add(tcsTrucktotcsConfirmqccpsend);
    }

    /**
     * 3.3.1.无人集卡进出栏申请
     *
     * @param obj obj
     * @return Result
     */
    public void tcsRtgTruck(JSONObject obj) {
        ////JSONObject response = tcsHttpApi.tcsRtgTruck(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("无人集卡进出栏申请truckNo{}", obj);
        TcsRtgTruck tcsRtgTruck = new TcsRtgTruck();
        tcsRtgTruck.setTime(new Date());
        tcsRtgTruck.setTruckNo(obj.getString("truckNo"));
        tcsRtgTruck.setToken(obj.getString("truckNo"));
        tcsRtgTruck.setParamTruck(obj.getString("truck"));
        tcsRtgTruck.setParamColumn(obj.getString("column"));
        tcsRtgTruck.setParamTime(obj.getString("time"));
        tcsRtgTruck.setParamType(obj.getString("type"));
        tcsRtgTruck.setParamDirection(obj.getString("direction"));
        tcsRtgTruck.setResCode(obj.getString("code"));
        tcsRtgTruck.setResMessage(obj.getString("messageType"));
        tcsRtgTruck.setResData(obj.getString("data"));
        tcsRtgTruckMapper.add(tcsRtgTruck);
    }

    /**
     * 3.3.2.无人集卡堆场内借道申请
     *
     * @param obj obj
     * @return Result
     */
    public void changeRoad(JSONObject obj) {
        log.info("无人集卡堆场内借道申请.obj{}",  obj);
        TcsRtgChangeRoad tcsRtgChangeRoad = new TcsRtgChangeRoad();
        tcsRtgChangeRoad.setTime(new Date());
        tcsRtgChangeRoad.setTruckNo(obj.getString("truckNo"));
        tcsRtgChangeRoad.setToken(obj.getString("truckNo"));
        tcsRtgChangeRoad.setParamTruck(obj.getString("truck"));
        tcsRtgChangeRoad.setParamTime(obj.getString("time"));
        tcsRtgChangeRoad.setParamTarget(obj.getString("target"));
        tcsRtgChangeRoad.setParamLane(obj.getString("lane"));
        tcsRtgChangeRoad.setResCode(obj.getString("code"));
        tcsRtgChangeRoad.setResMessage(obj.getString("messageType"));
        tcsRtgChangeRoad.setResData(obj.getString("data"));
        tcsRtgChangeRoadMapper.add(tcsRtgChangeRoad);
    }

    /**
     * 3.3.3.无人集卡堆场内到位上报
     *
     * @param obj obj
     * @return Result
     */
    public void uploadPosition(JSONObject obj) {
        //JSONObject response = tcsHttpApi.uploadPosition(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("无人集卡堆场内到位上报.truckNo{}", obj);
        TcsRtgPosition tcsRtgPosition = new TcsRtgPosition();
        tcsRtgPosition.setTime(new Date());
        tcsRtgPosition.setTruckNo(obj.getString("truckNo"));
        tcsRtgPosition.setToken(obj.getString("truckNo"));
        tcsRtgPosition.setParamEpno(obj.getString("epno"));
        tcsRtgPosition.setParamIsarrive(obj.getString("isarrive"));
        tcsRtgPosition.setParamEptype(obj.getString("eptype"));
        tcsRtgPosition.setParamArrivetime(obj.getString("arrivetime"));
        tcsRtgPosition.setParamPosition(obj.getString("position"));
        tcsRtgPosition.setResCode("200");
        tcsRtgPosition.setResMessage(obj.getString("messageType"));
        tcsRtgPosition.setResData(obj.getString("data"));
        tcsRtgPositionMapper.add(tcsRtgPosition);
    }

    /**
     * 3.3.4.无人集卡堆场内接收对位信息完成
     *
     * @param obj obj
     * @return Result
     */
    public void confirmCpsStop(JSONObject obj) {
        //JSONObject response = tcsHttpApi.confirmCpsStop(obj.getString("truckNo"), obj.getJSONObject("data"));
        log.info("无人集卡堆场内接收对位信息完成truckNo{}", obj);
        TcsRtgConfirmcpsstop tcsRtgConfirmcpsstop = new TcsRtgConfirmcpsstop();
        tcsRtgConfirmcpsstop.setTime(new Date());
        tcsRtgConfirmcpsstop.setTruckNo(obj.getString("truckNo"));
        tcsRtgConfirmcpsstop.setToken(obj.getString("truckNo"));
        tcsRtgConfirmcpsstop.setParamRtg(obj.getString("epno"));
        tcsRtgConfirmcpsstop.setParamStatus(obj.getString("isarrive"));
        tcsRtgConfirmcpsstop.setParamPosition(obj.getString("eptype"));
        tcsRtgConfirmcpsstop.setParamOrderid(obj.getString("arrivetime"));
        tcsRtgConfirmcpsstop.setParamTruck(obj.getString("arrivetime"));
        tcsRtgConfirmcpsstop.setParamTime(obj.getString("position"));
        tcsRtgConfirmcpsstop.setResCode("200");
        tcsRtgConfirmcpsstop.setResMessage(obj.getString("messageType"));
        tcsRtgConfirmcpsstop.setResData(obj.getString("data"));
        tcsRtgConfirmcpsstopMapper.add(tcsRtgConfirmcpsstop);
    }

    /**
     * 3.3.5.获取所有龙门吊GPS数据
     *
     * @param obj obj
     * @return Result
     */
    public void getLmdGpsAll(JSONObject obj) {
        //JSONObject response = tcsHttpApi.getLmdGpsAll(obj.getString("truckNo"));
        log.info("获取所有龙门吊GPS数据.truckNo{}",  obj);
        TcsGetLmdGpsAll tcsGetLmdGpsAll = new TcsGetLmdGpsAll();
        tcsGetLmdGpsAll.setTime(new Date());
        tcsGetLmdGpsAll.setTruckNo(obj.getString("truckNo"));
        tcsGetLmdGpsAll.setToken(obj.getString("truckNo"));
        tcsGetLmdGpsAll.setResCode("200");
        tcsGetLmdGpsAll.setResMessage(obj.getString("messageType"));
        tcsGetLmdGpsAll.setResData(obj.getString("data"));
        tcsGetLmdGpsAllMapper.add(tcsGetLmdGpsAll);
    }

    /**
     * 3.3.5.获取所有龙门吊GPS数据
     *
     * @param obj obj
     * @return Result
     */
    public void getLjGpsAll(JSONObject obj) {
        //JSONObject response = tcsHttpApi.getLjGpsAll(obj.getString("truckNo"));
        log.info("获取所有龙门吊GPS数据.truckNo{}", obj);
        TcsGetLjGpsAll tcsGetLjGpsAll = new TcsGetLjGpsAll();
        tcsGetLjGpsAll.setTime(new Date());
        tcsGetLjGpsAll.setTruckNo(obj.getString("truckNo"));
        tcsGetLjGpsAll.setToken(obj.getString("truckNo"));
        tcsGetLjGpsAll.setResCode("200");
        tcsGetLjGpsAll.setResMessage(obj.getString("messageType"));
        tcsGetLjGpsAll.setResData(obj.getString("data"));
        tcsGetLjGpsAllMapper.add(tcsGetLjGpsAll);
    }

   /**
     * 保存指令信息获取
     *
     * @param msg msg
     * @return Result
     */
    public void tosOrder(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketGetLmdDw tcsWebsocketGetLmdDw = new TcsWebsocketGetLmdDw();
        tcsWebsocketGetLmdDw.setTime(new Date());
        tcsWebsocketGetLmdDw.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetLmdDw.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketGetLmdDw.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketGetLmdDw.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketGetLmdDw.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketGetLmdDw.setResData(msg.getString("Data"));
        log.info("保存指令信息获取msg{},tcsWebsocketGetLmdDw{}", msg, tcsWebsocketGetLmdDw);
        tcsWebsocketGetLmdDwMapper.add(tcsWebsocketGetLmdDw);
    }

    /**
     * 保存桥吊CPS数据
     *
     * @param msg msg
     * @return Result
     */
    public void qdCpsInfo(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketGetQdGps tcsWebsocketGetQdGps = new TcsWebsocketGetQdGps();
        tcsWebsocketGetQdGps.setTime(new Date());
        tcsWebsocketGetQdGps.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetQdGps.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketGetQdGps.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketGetQdGps.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketGetQdGps.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketGetQdGps.setResData(msg.getString("Data"));
        log.info("保存桥吊CPS数据msg{},tcsWebsocketGetQdGps{}", msg, tcsWebsocketGetQdGps);
        tcsWebsocketGetQdGpsMapper.add(tcsWebsocketGetQdGps);
    }

    /**
     * 保存桥吊信息数据
     *
     * @param msg msg
     * @return Result
    */
    public void getChangedType(JSONObject msg) {
        // 保存至本地数据库
        log.info("保存桥吊信息数据{}", msg);
        TcsWebsocketGetQdInfo tcsWebsocketGetQdInfo = new TcsWebsocketGetQdInfo();
        tcsWebsocketGetQdInfo.setTime(new Date());
        tcsWebsocketGetQdInfo.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetQdInfo.setResChangedtype(msg.getString("MessageType"));
        tcsWebsocketGetQdInfo.setResId(msg.getString("Id"));
        tcsWebsocketGetQdInfo.setResName(msg.getString("Name"));
        tcsWebsocketGetQdInfo.setResState(msg.getIntValue("State"));
        tcsWebsocketGetQdInfo.setResSling(msg.getString("Sling"));
        tcsWebsocketGetQdInfo.setResPosition(msg.getString("Position"));
        tcsWebsocketGetQdInfo.setResClosedlanes(msg.getString("ClosedLanes"));
        tcsWebsocketGetQdInfo.setResRefreshtime(msg.getString("isMoving"));
        tcsWebsocketGetQdInfo.setResRefreshtimevalue(msg.getString("RefreshTimeValue"));
        log.info("保存桥吊信息数据{}tcsWebsocketGetQdInfo{}", msg, tcsWebsocketGetQdInfo);
        tcsWebsocketGetQdInfoMapper.add(tcsWebsocketGetQdInfo);
    }
    /**
     * 保存桥吊激光对位数据
     *
     * @param msg msg
     * @return Result
    */
    public void getQdBaysCpsInfo(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketGetQdDw tcsWebsocketGetQdDw = new TcsWebsocketGetQdDw();
        tcsWebsocketGetQdDw.setTime(new Date());
        tcsWebsocketGetQdDw.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetQdDw.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketGetQdDw.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketGetQdDw.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketGetQdDw.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketGetQdDw.setResData(msg.getString("Data"));
        log.info("保存桥吊激光对位数据{}tcsWebsocketGetQdDw{}", msg, tcsWebsocketGetQdDw);
        tcsWebsocketGetQdDwMapper.add(tcsWebsocketGetQdDw);
    }
    /**
     * 保存龙门吊对位数据
     *
     * @param msg msg
     * @return Result
     */
    public void getLmdCpsInfo(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketGetlmdInfo tcsWebsocketGetlmdInfo = new TcsWebsocketGetlmdInfo();
        tcsWebsocketGetlmdInfo.setTime(new Date());
        tcsWebsocketGetlmdInfo.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetlmdInfo.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketGetlmdInfo.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketGetlmdInfo.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketGetlmdInfo.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketGetlmdInfo.setResData(msg.getString("Data"));
        log.info("保存龙门吊对位数据{}tcsWebsocketGetlmdInfo{}", msg, tcsWebsocketGetlmdInfo);
        tcsWebsocketGetlmdInfoMapper.add(tcsWebsocketGetlmdInfo);
    }
    /**
     * 保存桥吊移动信息
     *
     * @param msg msg
     * @return Result
    */
    public void getQcMoveStatus(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketGetQdMv tcsWebsocketGetQdMv = new TcsWebsocketGetQdMv();
        tcsWebsocketGetQdMv.setTime(new Date());
        tcsWebsocketGetQdMv.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketGetQdMv.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketGetQdMv.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketGetQdMv.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketGetQdMv.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketGetQdMv.setResData(msg.getString("Data"));
        log.info("保存龙门吊对位数据{}tcsWebsocketGetQdMv{}", msg, tcsWebsocketGetQdMv);
        tcsWebsocketGetQdMvMapper.add(tcsWebsocketGetQdMv);
    }
    /**
     * 保存无人集卡接收作业完成可驶离信息
     *
     * @param msg msg
     * @return Result
    */
    public void getYardMove(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketTruckDriveAway tcsWebsocketTruckDriveAway = new TcsWebsocketTruckDriveAway();
        tcsWebsocketTruckDriveAway.setTime(new Date());
        tcsWebsocketTruckDriveAway.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketTruckDriveAway.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketTruckDriveAway.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketTruckDriveAway.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketTruckDriveAway.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketTruckDriveAway.setResData(msg.getString("Data"));
        log.info("保存龙门吊对位数据{}tcsWebsocketTruckDriveAway{}", msg, tcsWebsocketTruckDriveAway);
        tcsWebsocketTruckDriveAwayMapper.add(tcsWebsocketTruckDriveAway);
    }
    /**
     * 保存装卸锁可驶离信息
     *
     * @param msg msg
     * @return Result
    */
    public void getUnLock(JSONObject msg) {
        // 保存至本地数据库
        TcsWebsocketLockDriveAway tcsWebsocketLockDriveAway = new TcsWebsocketLockDriveAway();
        tcsWebsocketLockDriveAway.setTime(new Date());
        tcsWebsocketLockDriveAway.setTruckNo(msg.getString("truckNo"));
        tcsWebsocketLockDriveAway.setResReceiver(msg.getString("Receiver"));
        tcsWebsocketLockDriveAway.setResMessagetype(msg.getString("MessageType"));
        tcsWebsocketLockDriveAway.setResSendtime(msg.getString("SendTime"));
        tcsWebsocketLockDriveAway.setResMsgid(msg.getIntValue("MsgId"));
        tcsWebsocketLockDriveAway.setResData(msg.getString("Data"));
        log.info("保存龙门吊对位数据{}tcsWebsocketLockDriveAway{}", msg, tcsWebsocketLockDriveAway);
        tcsWebsocketLockDriveAwayMapper.add(tcsWebsocketLockDriveAway);
    }
}
