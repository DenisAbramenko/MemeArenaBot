package org.abr.memearenabot.bot.keyboard;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating inline keyboards
 */
public class InlineKeyboardFactory {
    
    private final TelegramBot bot;
    private final MessageService messageService;
    
    public InlineKeyboardFactory(TelegramBot bot, MessageService messageService) {
        this.bot = bot;
        this.messageService = messageService;
    }
    
    /**
     * Create inline keyboard for meme actions
     */
    public InlineKeyboardMarkup createMemeActionsKeyboard(String memeUrl) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
        // First row
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton publishButton = new InlineKeyboardButton();
        publishButton.setText(messageService.getMemeActionPublishMessage());
        publishButton.setCallbackData("publish:" + memeUrl);
        
        InlineKeyboardButton contestButton = new InlineKeyboardButton();
        contestButton.setText(messageService.getMemeActionContestMessage());
        contestButton.setCallbackData("contest:" + memeUrl);
        
        row1.add(publishButton);
        row1.add(contestButton);
        
        // Second row
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton nftButton = new InlineKeyboardButton();
        nftButton.setText(messageService.getMemeActionNftMessage());
        nftButton.setCallbackData("nft:" + memeUrl);
        
        InlineKeyboardButton newButton = new InlineKeyboardButton();
        newButton.setText(messageService.getMemeActionNewMessage());
        newButton.setCallbackData("new");
        
        row2.add(nftButton);
        row2.add(newButton);
        
        rowsInline.add(row1);
        rowsInline.add(row2);
        
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    
    /**
     * Create inline keyboard for template selection
     */
    public InlineKeyboardMarkup createTemplateSelectionKeyboard(List<String> templates) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
        for (String template : templates) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(template);
            button.setCallbackData("template:" + template);
            row.add(button);
            rowsInline.add(row);
        }
        
        // Add back button
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("↩ Back");
        backButton.setCallbackData("back");
        backRow.add(backButton);
        rowsInline.add(backRow);
        
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    
    /**
     * Create inline keyboard for voting
     */
    public InlineKeyboardMarkup createVoteKeyboard(Long memeId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton likeButton = new InlineKeyboardButton();
        likeButton.setText("👍 Like");
        likeButton.setCallbackData("vote:" + memeId);
        row.add(likeButton);
        
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    
    /**
     * Create inline keyboard for pagination
     */
    public InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String baseCommand) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        
        if (currentPage > 1) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("◀️ Previous");
            prevButton.setCallbackData(baseCommand + ":" + (currentPage - 1));
            row.add(prevButton);
        }
        
        InlineKeyboardButton pageButton = new InlineKeyboardButton();
        pageButton.setText(currentPage + " / " + totalPages);
        pageButton.setCallbackData("noop");
        row.add(pageButton);
        
        if (currentPage < totalPages) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Next ▶️");
            nextButton.setCallbackData(baseCommand + ":" + (currentPage + 1));
            row.add(nextButton);
        }
        
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
} 