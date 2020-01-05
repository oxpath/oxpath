/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ox.cs.diadem.webapi.dom;

import com.google.common.base.CaseFormat;

/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMCSS2Properties {

  public static enum CssProp {
    font_family, font_size, font_style, font_variant, font_weight, line_height, display, text_align, vertical_align, visibility;

    public String getPropertyName() {
      return propertyName;
    }

    private final String propertyName = name().replace("_", "-");
  }

  // public static enum CssProp {
  // background_color,
  // background_image, background_position, background_repeat,
  // border_bottom,
  // border_bottom_color, border_bottom_style, border_bottom_width,
  // border_left, border_left_color, border_left_style, border_left_width,
  // border_right, border_right_color,
  // border_right_style, border_right_width, border_spacing, border_top,
  // border_top_color,
  // border_top_style, border_top_width, bottom, color,
  // content, cursor, direction, display,
  // empty_cells, font_family, font_size, font_size_adjust, font_stretch,
  // font_style,
  // font_variant, font_weight, height, left, letter_spacing, line_height,
  // list_style_type, margin_bottom, margin_left, margin_right, margin_top,
  // max_height, max_width, min_height, min_width, padding_bottom,
  // padding_left, padding_right, padding_top,
  // position, /*quotes,*/ right, size, table_layout, text_align,
  // text_decoration, text_indent, top, vertical_align,
  // visibility, width, word_spacing, z_index;
  public static enum CssProperty {
    accelerator, azimuth, background, background_attachment, background_color, background_image, background_position, background_repeat, border, border_bottom, border_bottom_color, border_bottom_style, border_bottom_width, border_collapse, border_color, border_left, border_left_color, border_left_style, border_left_width, border_right, border_right_color, border_right_style, border_right_width, border_spacing, border_style, border_top, border_top_color, border_top_style, border_top_width, border_width, bottom, caption_side, clear, clip, color, content, counter_increment, counter_reset, cue, cue_after, cue_before, cursor, direction, display, elevation, empty_cells, font, font_family, font_size, font_size_adjust, font_stretch, font_style, font_variant, font_weight, height, left, letter_spacing, line_height, list_style, list_style_image, list_style_position, list_style_type, margin, margin_bottom, margin_left, margin_right, margin_top, marker_offset, marks, max_height, max_width, min_height, min_width, orphans, outline, outline_color, outline_style, outline_width, overflow, padding, padding_bottom, padding_left, padding_right, padding_top,

    page, page_break_after, page_break_before, page_break_inside, pause, pause_after, pause_before, pitch, pitch_range, position, richness, right, size, speak, speak_header, speak_numeral, speak_punctuation, speech_rate, stress, table_layout, text_align, text_decoration, text_indent, text_shadow, text_transform, top, unicode_bidi, vertical_align, visibility, voice_family, volume, white_space, widows, width, word_spacing, z_index;// quotes;

    public String getPropertyName() {
      return propertyName;
    }

    private final String propertyName = name().replace("_", "-");

    public String asLowerCamelCase() {

      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
    }
  }
}
