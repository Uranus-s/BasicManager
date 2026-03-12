import com.basic.dao.sysUser.mapper.SysUserMapper;
import com.basic.sericve.sysUser.service.ISysUserService;
import com.basic.web.WebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {WebApplication.class})
public class MybatisTest {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void test() {
        System.out.println("sysUserMapper = " + sysUserMapper);
        System.out.println(sysUserMapper.getUserByName2("1"));// mapper Select
        System.out.println(sysUserMapper.getUserByName1("1"));// @Select
        System.out.println(sysUserMapper.selectById(1));// plus select
        System.out.println(sysUserService.getById(1));
    }
}
