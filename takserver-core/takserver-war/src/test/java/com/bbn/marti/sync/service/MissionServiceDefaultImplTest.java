package com.bbn.marti.sync.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bbn.marti.remote.groups.Group;
import com.bbn.marti.remote.groups.GroupManager;
import com.bbn.marti.sync.model.Mission;
import com.bbn.marti.sync.model.MissionRole;
import com.bbn.marti.sync.repository.MissionRepository;
import com.bbn.marti.sync.repository.MissionChangeRepository;
import com.bbn.marti.sync.repository.ResourceRepository;
import com.bbn.marti.sync.repository.LogEntryRepository;
import com.bbn.marti.remote.SubscriptionManagerLite;
import com.bbn.marti.remote.util.RemoteUtil;
import com.bbn.marti.sync.EnterpriseSyncService;
import com.bbn.marti.dao.kml.JDBCCachingKMLDao;
import com.bbn.marti.service.kml.KMLService;
import com.bbn.marti.remote.SubmissionInterface;
import com.bbn.marti.sync.repository.ExternalMissionDataRepository;
import com.bbn.marti.sync.repository.MissionInvitationRepository;
import com.bbn.marti.sync.repository.MissionSubscriptionRepository;
import com.bbn.marti.sync.repository.MissionFeedRepository;
import com.bbn.marti.sync.repository.DataFeedRepository;
import com.bbn.marti.sync.repository.MissionLayerRepository;
import com.bbn.marti.maplayer.MapLayerService;
import com.bbn.marti.sync.repository.MissionRoleRepository;
import tak.server.cache.CoTCacheHelper;
import tak.server.cache.MissionCacheHelper;
import com.bbn.marti.classification.service.ClassificationService;
import tak.server.filemanager.FileManagerService;
import tak.server.ignite.grid.SubscriptionManagerProxyHandler;

@RunWith(MockitoJUnitRunner.class)
public class MissionServiceDefaultImplTest {

    @Mock private DataSource dataSource;
    @Mock private MissionRepository missionRepository;
    @Mock private MissionChangeRepository missionChangeRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private LogEntryRepository logEntryRepository;
    @Mock private SubscriptionManagerLite subscriptionManager;
    @Mock private SubscriptionManagerProxyHandler subscriptionManagerProxy;
    @Mock private RemoteUtil remoteUtil;
    @Mock private EnterpriseSyncService syncStore;
    @Mock private JDBCCachingKMLDao kmlDao;
    @Mock private KMLService kmlService;
    @Mock private SubmissionInterface submission;
    @Mock private ExternalMissionDataRepository externalMissionDataRepository;
    @Mock private MissionInvitationRepository missionInvitationRepository;
    @Mock private MissionSubscriptionRepository missionSubscriptionRepository;
    @Mock private MissionFeedRepository missionFeedRepository;
    @Mock private DataFeedRepository dataFeedRepository;
    @Mock private MissionLayerRepository missionLayerRepository;
    @Mock private MapLayerService mapLayerService;
    @Mock private GroupManager groupManager;
    @Mock private MissionRoleRepository missionRoleRepository;
    @Mock private CoTCacheHelper cotCacheHelper;
    @Mock private MissionCacheHelper missionCacheHelper;
    @Mock private ClassificationService classificationService;
    @Mock private FileManagerService fileManagerService;

    private MissionServiceDefaultImpl missionService;

    @Before
    public void setup() {
        missionService = new MissionServiceDefaultImpl(
            dataSource,
            missionRepository,
            missionChangeRepository,
            resourceRepository,
            logEntryRepository,
            subscriptionManager,
            subscriptionManagerProxy,
            remoteUtil,
            null, // Marshaller not needed for these tests
            syncStore,
            kmlDao,
            kmlService,
            submission,
            externalMissionDataRepository,
            missionInvitationRepository,
            missionSubscriptionRepository,
            missionFeedRepository,
            dataFeedRepository,
            missionLayerRepository,
            mapLayerService,
            groupManager,
            null, // CacheManager not needed for these tests
            null, // CommonUtil not needed for these tests
            missionRoleRepository,
            cotCacheHelper,
            missionCacheHelper,
            classificationService,
            fileManagerService
        );
    }

    @Test
    public void testCreateMission() {
        // Arrange
        String missionName = "TestMission";
        String creatorUid = "testCreator";
        String groupVector = "group1,group2";
        String description = "Test mission description";
        String chatRoom = "testChatRoom";
        String tool = "public";
        String baseLayer = "testLayer";
        String bbox = null;
        String path = null;
        boolean defaultRole = true;
        boolean expiration = false;
        Long expireSeconds = null;
        String password = null;
        
        Mission expectedMission = new Mission();
        expectedMission.setName(missionName);
        expectedMission.setCreatorUid(creatorUid);
        expectedMission.setDescription(description);
        expectedMission.setChatRoom(chatRoom);
        expectedMission.setTool(tool);
        expectedMission.setBaseLayer(baseLayer);
        expectedMission.setDefaultRole(defaultRole);
        
        when(missionRepository.save(any(Mission.class))).thenReturn(expectedMission);
        when(groupManager.findGroups(anyString())).thenReturn(null);
        
        // Act
        Mission result = missionService.createMission(
            missionName, creatorUid, groupVector, description, chatRoom,
            tool, baseLayer, bbox, path, defaultRole, expiration, expireSeconds, password
        );
        
        // Assert
        assertNotNull("Created mission should not be null", result);
        assertEquals("Mission name should match", missionName, result.getName());
        assertEquals("Creator UID should match", creatorUid, result.getCreatorUid());
        assertEquals("Description should match", description, result.getDescription());
        assertEquals("Chat room should match", chatRoom, result.getChatRoom());
        assertEquals("Tool should match", tool, result.getTool());
        assertEquals("Base layer should match", baseLayer, result.getBaseLayer());
        assertEquals("Default role should match", defaultRole, result.isDefaultRole());
        
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    public void testDeleteMission() {
        // Arrange
        String missionName = "TestMission";
        String creatorUid = "testCreator";
        String groupVector = "group1,group2";
        boolean deepDelete = true;
        
        Mission existingMission = new Mission();
        existingMission.setName(missionName);
        existingMission.setCreatorUid(creatorUid);
        
        when(missionRepository.findByNameNoMission(missionName)).thenReturn(existingMission);
        when(groupManager.findGroups(anyString())).thenReturn(null);
        
        // Act
        Mission result = missionService.deleteMission(missionName, creatorUid, groupVector, deepDelete);
        
        // Assert
        assertNotNull("Deleted mission should not be null", result);
        assertEquals("Mission name should match", missionName, result.getName());
        assertEquals("Creator UID should match", creatorUid, result.getCreatorUid());
        
        verify(missionRepository).findByNameNoMission(missionName);
        verify(missionRepository).delete(any(Mission.class));
    }

    @Test
    public void testGetMission() {
        // Arrange
        String missionName = "TestMission";
        String groupVector = "group1,group2";
        boolean hydrateDetails = true;
        
        Mission expectedMission = new Mission();
        expectedMission.setName(missionName);
        
        when(missionRepository.findByNameNoMission(missionName)).thenReturn(expectedMission);
        when(groupManager.findGroups(anyString())).thenReturn(null);
        
        // Act
        Mission result = missionService.getMission(missionName, groupVector);
        
        // Assert
        assertNotNull("Retrieved mission should not be null", result);
        assertEquals("Mission name should match", missionName, result.getName());
        
        verify(missionRepository).findByNameNoMission(missionName);
    }

    @Test
    public void testGetAllMissions() {
        // Arrange
        boolean passwordProtected = false;
        boolean defaultRole = true;
        String tool = "public";
        NavigableSet<Group> requestGroups = null;
        
        List<Mission> expectedMissions = Arrays.asList(
            new Mission("Mission1", "creator1"),
            new Mission("Mission2", "creator2")
        );
        
        when(missionRepository.findAll()).thenReturn(expectedMissions);
        
        // Act
        List<Mission> result = missionService.getAllMissions(passwordProtected, defaultRole, tool, requestGroups);
        
        // Assert
        assertNotNull("Mission list should not be null", result);
        assertEquals("Mission list size should match", 2, result.size());
        assertEquals("First mission name should match", "Mission1", result.get(0).getName());
        assertEquals("Second mission name should match", "Mission2", result.get(1).getName());
        
        verify(missionRepository).findAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMissionWithInvalidName() {
        // Arrange
        String missionName = "";  // Invalid empty name
        String creatorUid = "testCreator";
        String groupVector = "group1,group2";
        String description = "Test mission description";
        String chatRoom = "testChatRoom";
        String tool = "public";
        String baseLayer = "testLayer";
        String bbox = null;
        String path = null;
        boolean defaultRole = true;
        boolean expiration = false;
        Long expireSeconds = null;
        String password = null;
        
        // Act - should throw IllegalArgumentException
        missionService.createMission(
            missionName, creatorUid, groupVector, description, chatRoom,
            tool, baseLayer, bbox, path, defaultRole, expiration, expireSeconds, password
        );
    }
}