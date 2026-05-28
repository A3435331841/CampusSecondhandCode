package com.campus.secondhand.service.impl;

import com.campus.secondhand.dto.StudentVerifyDTO;
import com.campus.secondhand.entity.StudentRoster;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.StudentRosterMapper;
import com.campus.secondhand.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentVerificationServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StudentRosterMapper studentRosterMapper;

    @InjectMocks
    private StudentVerificationServiceImpl studentVerificationService;

    @Test
    void verifyStudent_shouldMarkUserVerifiedWhenRosterMatchesExactly() {
        StudentVerifyDTO dto = new StudentVerifyDTO();
        dto.setRealName("Zhang Chen");
        dto.setStudentNo("2023123401");
        dto.setCollege("School of Computer Science");
        dto.setMajor("Software Engineering");
        dto.setGrade("2023");
        dto.setIdCardSuffix("1234");

        User currentUser = new User();
        currentUser.setId(18L);
        currentUser.setVerifyStatus("UNVERIFIED");

        StudentRoster roster = new StudentRoster();
        roster.setId(1L);
        roster.setRealName("Zhang Chen");
        roster.setStudentNo("2023123401");
        roster.setCollege("School of Computer Science");
        roster.setMajor("Software Engineering");
        roster.setGrade("2023");
        roster.setIdCardSuffix("1234");
        roster.setStatus(1);

        when(userMapper.selectById(18L)).thenReturn(currentUser);
        when(studentRosterMapper.selectOne(any())).thenReturn(roster);

        User verifiedUser = studentVerificationService.verifyStudent(18L, dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(18L, savedUser.getId());
        assertEquals("Zhang Chen", savedUser.getRealName());
        assertEquals("2023123401", savedUser.getStudentNo());
        assertEquals("School of Computer Science", savedUser.getCollege());
        assertEquals("Software Engineering", savedUser.getMajor());
        assertEquals("2023", savedUser.getGrade());
        assertEquals("1234", savedUser.getIdCardSuffix());
        assertEquals("VERIFIED", savedUser.getVerifyStatus());
        assertNotNull(savedUser.getVerifyTime());

        assertEquals("VERIFIED", verifiedUser.getVerifyStatus());
        assertEquals("Zhang Chen", verifiedUser.getRealName());
        assertNotNull(verifiedUser.getVerifyTime());
    }

    @Test
    void verifyStudent_shouldRejectMismatchWithoutUpdatingUserProfile() {
        StudentVerifyDTO dto = new StudentVerifyDTO();
        dto.setRealName("Li Ming");
        dto.setStudentNo("2023123402");
        dto.setCollege("School of Computer Science");
        dto.setMajor("Artificial Intelligence");
        dto.setGrade("2023");
        dto.setIdCardSuffix("8888");

        User currentUser = new User();
        currentUser.setId(19L);
        currentUser.setVerifyStatus("UNVERIFIED");

        when(userMapper.selectById(19L)).thenReturn(currentUser);
        when(studentRosterMapper.selectOne(any())).thenReturn(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> studentVerificationService.verifyStudent(19L, dto));

        assertEquals("学籍认证失败", exception.getMessage());
        verify(studentRosterMapper).selectOne(any());
        verify(userMapper, never()).updateById(any(User.class));
        assertEquals("UNVERIFIED", currentUser.getVerifyStatus());
    }

    @Test
    void verifyStudent_shouldRejectMissingUser() {
        StudentVerifyDTO dto = new StudentVerifyDTO();
        dto.setRealName("Wang Rui");
        dto.setStudentNo("2022121876");
        dto.setCollege("School of Business");
        dto.setMajor("Marketing");
        dto.setGrade("2022");
        dto.setIdCardSuffix("5678");

        when(userMapper.selectById(20L)).thenReturn(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> studentVerificationService.verifyStudent(20L, dto));

        assertEquals("用户不存在", exception.getMessage());
        verify(studentRosterMapper, never()).selectOne(any());
        verify(userMapper, never()).updateById(any(User.class));
    }
}
