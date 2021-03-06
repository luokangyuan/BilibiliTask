package top.srcrs.bilibili;

import com.alibaba.fastjson.JSONObject;
import top.srcrs.bilibili.domain.Data;
import top.srcrs.bilibili.util.PackageScanner;
import top.srcrs.bilibili.util.ReadConfig;
import top.srcrs.bilibili.util.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 启动类，程序运行开始的地方
 * @author srcrs
 * @Time 2020-10-13
 */
public class BiliStart {
    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(BiliStart.class);
    private static Data data = Data.getInstance();

    public static void main(String[] args) {
        if(args.length==0){
            LOGGER.error("请在Github Secrets中添加你的Cookie信息");
        }
        data.setCookie(args[0],args[1],args[2]);
        /** 读取yml文件配置信息 */
        ReadConfig.transformation("/config.yml");
        if(check()){
            LOGGER.info("用户名: {}",data.getUname());
            LOGGER.info("硬币: {}",data.getMoney());
            LOGGER.info("经验: {}",data.getCurrentExp());
            PackageScanner pack = new PackageScanner() {
                @Override
                public void dealClass(Class<?> klass) {
                    try{
                        Object object = klass.newInstance();
                        Class clazz = object.getClass();
                        Method method = clazz.getMethod("run",null);
                        method.invoke(object);
                    } catch (Exception e){
                        LOGGER.error("反射获取对象错误 -- "+e);
                    }
                }
            };
            /** 动态执行task包下的所有java代码 */
            pack.scannerPackage("top.srcrs.bilibili.task");
            LOGGER.info("本次任务运行完毕。");
        } else {
            throw  new RuntimeException("账户已失效，请在Secrets重新绑定你的信息");
        }
    }

    /**
     * 检查用户的状态
     * @return boolean
     * @author srcrs
     * @Time 2020-10-13
     */
    public static boolean check(){
        JSONObject jsonObject = Request.get("https://api.bilibili.com/x/web-interface/nav");
        JSONObject object = jsonObject.getJSONObject("data");
        if("0".equals(jsonObject.getString("code"))){
            /** 用户名 */
            data.setUname(object.getString("uname"));
            /** 账户的uid */
            data.setMid(object.getString("mid"));
            /** vip类型 */
            data.setVipType(object.getString("vipType"));
            /** 硬币数 */
            data.setMoney(object.getString("money"));
            /** 经验 */
            data.setCurrentExp(object.getJSONObject("level_info").getString("current_exp"));
            /** 大会员状态 */
            data.setVipStatus(object.getString("vipStatus"));
            /** 钱包B币卷余额 */
            data.setCoupon_balance(object.getJSONObject("wallet").getString("coupon_balance"));
            return true;
        }
        return false;
    }
}
