package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserMapper userMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userMapper", userMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listUsers_shouldRejectNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/user/list")
                        .requestAttr("currentUserRole", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(500)))
                .andExpect(jsonPath("$.msg", is("无权限操作")));

        verify(userMapper, never()).selectPage(any(), any());
    }

    @Test
    void listUsers_shouldAllowAdminUsers() throws Exception {
        when(userMapper.selectPage(any(), any())).thenReturn(new Page<User>());

        mockMvc.perform(get("/api/user/list")
                        .requestAttr("currentUserRole", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)));

        verify(userMapper).selectPage(any(), any());
    }
}
