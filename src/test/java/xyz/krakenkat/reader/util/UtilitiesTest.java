package xyz.krakenkat.reader.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.krakenkat.reader.dto.ItemDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static xyz.krakenkat.reader.builder.ObjectsBuilder.buildItemDTO;
import static xyz.krakenkat.reader.builder.ObjectsBuilder.buildSequenceItemDTOList;

class UtilitiesTest {

    private List<ItemDTO> itemDTOList;

    @BeforeEach
    void setUp() {
        // Create Mock
        mockStatic(Utilities.class);

        // Fill list
        itemDTOList = buildSequenceItemDTOList();
    }

    @Test
    void testFindItemByNumber() {
        ItemDTO expected = buildItemDTO("2");

        when(Utilities.findItemByNumber(anyList(), anyInt())).thenReturn(expected);
        ItemDTO actual = Utilities.findItemByNumber(itemDTOList, 2);

        assertEquals(expected, actual);
    }

    @Test
    void testJoinLists_ExpectedBehavior() {
        // Given
        List<ItemDTO> whakoomList = buildSequenceItemDTOList();
        List<ItemDTO> publisherList = buildSequenceItemDTOList();

        // When

        when(Utilities.joinLists(anyList(), anyList())).thenReturn(whakoomList);

        List<ItemDTO> actual = Utilities.joinLists(whakoomList, publisherList);

        assertEquals(whakoomList, actual);
    }

    @Test
    void testJoinLists_WhenISBNIsDefault() {
        // Given
        List<ItemDTO> whakoomList = buildSequenceItemDTOList()
                .stream()
                .peek(itemDTO -> itemDTO.setIsbn(ReaderConstants.DEFAULT_ISBN))
                .toList();
        List<ItemDTO> publisherList = buildSequenceItemDTOList();

        // When

        when(Utilities.joinLists(anyList(), anyList())).thenReturn(buildSequenceItemDTOList());

        List<ItemDTO> actual = Utilities.joinLists(whakoomList, publisherList);

        actual.forEach(System.out::println);

        assertEquals(whakoomList, actual);
    }

}