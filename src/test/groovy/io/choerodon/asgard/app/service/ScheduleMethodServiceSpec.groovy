package io.choerodon.asgard.app.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.ScheduleMethodParams
import io.choerodon.asgard.app.service.impl.ScheduleMethodServiceImpl
import io.choerodon.asgard.infra.dto.QuartzMethodDTO
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper
import io.choerodon.core.exception.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleMethodServiceSpec extends Specification {
    @Autowired
    ScheduleMethodServiceImpl scheduleMethodService
    private QuartzMethodMapper mockMethodMapper = Mock(QuartzMethodMapper)

    void setup() {
        scheduleMethodService.setMethodMapper(mockMethodMapper)
    }

    def "PageQuery"() {
        given: "参数准备"
        def code = "code"
        def service = "service"
        def method = "method"
        def description = "description"
        def params = "params"

        and: "构造mock返回结果"
        def quartzMethod01 = new QuartzMethodDTO()
        quartzMethod01.setId(1L)
        quartzMethod01.setCode(code)
        quartzMethod01.setService(service)
        quartzMethod01.setMethod(method)
        quartzMethod01.setDescription(description)

        def list = new ArrayList<QuartzMethodDTO>()
        list.add(quartzMethod01)
        and: "mock"
        DiscoveryClient dc = Mock(DiscoveryClient)
        scheduleMethodService.setDiscoveryClient(dc)
        mockMethodMapper.fulltextSearch(_, _, _, _, _) >> { return list }
        dc.getInstances(_) >> { new ArrayList<>() }
        when: "方法调用"
        scheduleMethodService.pageQuery(1,2, code, service, method, description, params, "site")
        then: "结果分析"
        noExceptionThrown()
    }

    def "GetMethodByService"() {
        given: "参数准备"
        def serviceName = "serviceName"
        and: "构造mock返回结果"
        def quartzMethod = new QuartzMethodDTO()
        quartzMethod.setId(1L)
        quartzMethod.setMethod("method")
        quartzMethod.setCode("code")
        quartzMethod.setParams("[]")
        def list = new ArrayList<QuartzMethodDTO>()
        list.add(quartzMethod)
        and: "mock"
        mockMethodMapper.selectByService(_, "site") >> { return list }
        when: "方法调用"
        def query = scheduleMethodService.getMethodByService(serviceName, "site")
        then: "结果分析"
        noExceptionThrown()
        query.size() == list.size()
    }

    def "GetParams[MethodNotExist]"() {
        given: "参数准备"
        def id = 1L
        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return null }
        when: "方法调用"
        scheduleMethodService.getParams(id, "site")
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleMethod.notExist"
    }

    def "GetParams"() {
        given: "参数准备"
        def id = 1L
        def quartzMethod = new QuartzMethodDTO()
        quartzMethod.setId(1L)
        quartzMethod.setLevel("site")
        def scheduleMethodParamsDTO = new ScheduleMethodParams()
        scheduleMethodParamsDTO.setId(1L)
        scheduleMethodParamsDTO.setParamsJson("[{\"name\":\"isInstantly\",\"defaultValue\":true,\"type\":\"Boolean\",\"description\":\"测试用布尔类型字段\"},{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")

        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return quartzMethod }
        mockMethodMapper.selectParamsById(id) >> { return scheduleMethodParamsDTO }

        when: "方法调用"
        scheduleMethodService.getParams(id, "site")

        then: "结果分析"
        noExceptionThrown()
    }

    def "GetParams[jsonIOException]"() {
        given: "参数准备"
        def id = 1L
        def quartzMethod = new QuartzMethodDTO()
        quartzMethod.setId(1L)
        quartzMethod.setLevel("site")
        def scheduleMethodParamsDTO = new ScheduleMethodParams()
        scheduleMethodParamsDTO.setId(1L)
        scheduleMethodParamsDTO.setParamsJson("[invalid]")
        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return quartzMethod }
        mockMethodMapper.selectParamsById(id) >> { return scheduleMethodParamsDTO }

        when: "方法调用"
        scheduleMethodService.getParams(id, "site")

        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.ScheduleMethodParams.jsonIOException"
    }
}
