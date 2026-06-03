package org.openpnp.machine.photon.sheets.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.Translations;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.IdentifiableListCellRenderer;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.PartsComboBoxModel;
import org.openpnp.machine.photon.PhotonFeeder;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.openpnp.spi.Camera;
import org.openpnp.util.MovableUtils;
import org.openpnp.spi.Nozzle;
import org.openpnp.util.UiUtils;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.ui.CvPipelineEditor;
import org.openpnp.vision.pipeline.ui.CvPipelineEditorDialog;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class FeederConfigurationWizard extends AbstractConfigurationWizard {
	private final PhotonFeeder feeder;
	
	private final JLabel hardwareIdValue;
	private final JLabel slotAddressValue;
	private final JComboBox partCb;
	private final JTextField partPitchTf;
	private final JTextField feedRetryCountTf;
	private final JTextField pickRetryCountTf;
	private final JCheckBox useVisionCheckbox;
	private final JCheckBox usePartVisionCheckbox;
	private final JTextField varianceHistoryTf;
	private final JTextField skippedCalibrationTf;
	private final JTextField xSlotTf;
	private final JTextField ySlotTf;
	private final JTextField zSlotTf;
	private final JTextField rotSlotTf;
	private final JTextField xOffsetTf;
	private final JTextField yOffsetTf;
	private final JTextField zOffsetTf;
	private final JTextField rotOffsetTf;
	private final LocationButtonsPanel offsetLocationPanel;
	private final JLabel moveWhileFeedingLabel;
	private final JCheckBox moveWhileFeedingCheckBox;
	private final LocationButtonsPanel slotLocationPanel;

	/**
	 * Create the panel.
	 */
	public FeederConfigurationWizard(PhotonFeeder feeder) {
		this.feeder = feeder;
		
		JPanel infoPanel = new JPanel();
		contentPanel.add(infoPanel);
		infoPanel.setBorder(new TitledBorder(null, Translations.getString("FeederConfigurationWizard.InfoPanel.Border.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		infoPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(50dlu;pref)"), //$NON-NLS-1$
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.BUTTON_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.PREF_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel hardwareIdLabel = new JLabel(Translations.getString("FeederConfigurationWizard.InfoPanel.hardwareIdLabel.text")); //$NON-NLS-1$
		infoPanel.add(hardwareIdLabel, "2, 2, left, center"); //$NON-NLS-1$
		
		hardwareIdValue = new JLabel(""); //$NON-NLS-1$
		infoPanel.add(hardwareIdValue, "4, 2, 5, 1, left, center"); //$NON-NLS-1$
		
		JLabel slotAddressLabel = new JLabel(Translations.getString("FeederConfigurationWizard.InfoPanel.slotAddressLabel.text")); //$NON-NLS-1$
		infoPanel.add(slotAddressLabel, "2, 4"); //$NON-NLS-1$

		slotAddressValue = new JLabel(""); //$NON-NLS-1$
		infoPanel.add(slotAddressValue, "4, 4"); //$NON-NLS-1$

		JButton findButton = new JButton(findSlotAddressAction);
		infoPanel.add(findButton, "6, 4"); //$NON-NLS-1$
		
		JPanel partPanel = new JPanel();
		partPanel.setBorder(new TitledBorder(null, Translations.getString("FeederConfigurationWizard.PartPanel.Border.title"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$
		contentPanel.add(partPanel);
		partPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.BUTTON_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(50dlu;default)"), //$NON-NLS-1$
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel partLabel = new JLabel(Translations.getString("FeederConfigurationWizard.PartPanel.partLabel.text")); //$NON-NLS-1$
		partPanel.add(partLabel, "2, 2, right, default"); //$NON-NLS-1$

		partCb = new JComboBox();
		partPanel.add(partCb, "4, 2, 5, 1, fill, default"); //$NON-NLS-1$
		partCb.setModel(new PartsComboBoxModel());
        partCb.setRenderer(new IdentifiableListCellRenderer<Part>());
		
		JLabel partPitchLabel = new JLabel(Translations.getString("FeederConfigurationWizard.PartPanel.partPitchLabel.text")); //$NON-NLS-1$
		partPanel.add(partPitchLabel, "2, 4, right, default"); //$NON-NLS-1$
		
		partPitchTf = new JTextField();
		partPanel.add(partPitchTf, "4, 4, fill, default"); //$NON-NLS-1$
		partPitchTf.setColumns(10);
		
		JButton feedButton = new JButton(feedAction);
		partPanel.add(feedButton, "6, 4"); //$NON-NLS-1$

		JButton feedOneMmButton = new JButton(feedOneMmAction);
		partPanel.add(feedOneMmButton, "8, 4"); //$NON-NLS-1$
		
		JLabel feedRetryLabel = new JLabel(Translations.getString("FeederConfigurationWizard.PartPanel.feedRetryLabel.text")); //$NON-NLS-1$
		partPanel.add(feedRetryLabel, "2, 6, right, default"); //$NON-NLS-1$
		
		feedRetryCountTf = new JTextField();
		partPanel.add(feedRetryCountTf, "4, 6, fill, default"); //$NON-NLS-1$
		feedRetryCountTf.setColumns(10);
		
		JLabel pickRetryLabel = new JLabel(Translations.getString("FeederConfigurationWizard.PartPanel.pickRetryLabel.text")); //$NON-NLS-1$
		partPanel.add(pickRetryLabel, "2, 8, right, default"); //$NON-NLS-1$
		
		pickRetryCountTf = new JTextField();
		partPanel.add(pickRetryCountTf, "4, 8, fill, default"); //$NON-NLS-1$
		pickRetryCountTf.setColumns(10);
		
		final JPanel visionPanel = new JPanel();
		visionPanel.setBorder(new TitledBorder(
			null,
			Translations.getString("FeederConfigurationWizard.VisionPanel.Border.title"),
			TitledBorder.LEADING,
			TitledBorder.TOP
		));
		contentPanel.add(visionPanel);
		visionPanel.setLayout(new FormLayout(
			new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
			},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
			}
		));

		final JLabel useVisionLabel = new JLabel(
			Translations.getString("FeederConfigurationWizard.VisionPanel.useVisionLabel.text")
		);
		visionPanel.add(useVisionLabel, "2, 2");

		useVisionCheckbox = new JCheckBox();
		visionPanel.add(useVisionCheckbox, "4, 2");

		final JButton positionCameraButton = new JButton(positionCameraAction);
		visionPanel.add(positionCameraButton, "6, 2");

		final JButton editPipelineButton = new JButton(editPipelineAction);
		visionPanel.add(editPipelineButton, "2, 4");

		final JButton resetPipelineButton = new JButton(resetPipelineAction);
		visionPanel.add(resetPipelineButton, "4, 4");

		final JLabel usePartVisionLabel = new JLabel(
			Translations.getString("FeederConfigurationWizard.VisionPanel.usePartVisionLabel.text")
		);
		visionPanel.add(usePartVisionLabel, "2, 6");

		usePartVisionCheckbox = new JCheckBox();
		visionPanel.add(usePartVisionCheckbox, "4, 6");

		final JButton editPartPipelineButton = new JButton(editPartPipelineAction);
		visionPanel.add(editPartPipelineButton, "2, 8");

		final JButton resetPartPipelineButton = new JButton(resetPartPipelineAction);
		visionPanel.add(resetPartPipelineButton, "4, 8");

		final JLabel varianceHistoryLabel = new JLabel(
			Translations.getString("FeederConfigurationWizard.VisionPanel.varianceHistoryLabel.text")
		);
		visionPanel.add(varianceHistoryLabel, "2, 10, right, default");

		varianceHistoryTf = new JTextField();
		visionPanel.add(varianceHistoryTf, "4, 10, fill, default");
		varianceHistoryTf.setColumns(10);

		final JLabel skippedCalibrationLabel = new JLabel(
			Translations.getString("FeederConfigurationWizard.VisionPanel.skippedCalibrationLabel.text")
		);
		visionPanel.add(skippedCalibrationLabel, "2, 12, right, default");

		skippedCalibrationTf = new JTextField();
		visionPanel.add(skippedCalibrationTf, "4, 12, fill, default");
		skippedCalibrationTf.setColumns(10);

		JPanel locationPanel = new JPanel();
		locationPanel.setBorder(new TitledBorder(null, Translations.getString("FeederConfigurationWizard.LocationPanel.Border.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		contentPanel.add(locationPanel);
		locationPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JLabel xOffsetLabel = new JLabel("X"); //$NON-NLS-1$
		locationPanel.add(xOffsetLabel, "4, 2, center, center"); //$NON-NLS-1$
		
		JLabel yOffsetLabel = new JLabel("Y"); //$NON-NLS-1$
		locationPanel.add(yOffsetLabel, "6, 2, center, center"); //$NON-NLS-1$
		
		JLabel zOffsetLabel = new JLabel("Z"); //$NON-NLS-1$
		locationPanel.add(zOffsetLabel, "8, 2, center, center"); //$NON-NLS-1$
		
		JLabel rotationOffsetLabel = new JLabel(Translations.getString("FeederConfigurationWizard.LocationPanel.rotationOffsetLabel.text")); //$NON-NLS-1$
		locationPanel.add(rotationOffsetLabel, "10, 2, center, center"); //$NON-NLS-1$
		
		JLabel slotLocationLabel = new JLabel(Translations.getString("FeederConfigurationWizard.LocationPanel.slotLocationLabel.text")); //$NON-NLS-1$
		locationPanel.add(slotLocationLabel, "2, 4, right, center"); //$NON-NLS-1$
		
		xSlotTf = new JTextField();
		locationPanel.add(xSlotTf, "4, 4, left, center"); //$NON-NLS-1$
		xSlotTf.setColumns(10);
		
		ySlotTf = new JTextField();
		locationPanel.add(ySlotTf, "6, 4, left, center"); //$NON-NLS-1$
		ySlotTf.setColumns(10);
		
		zSlotTf = new JTextField();
		locationPanel.add(zSlotTf, "8, 4, left, center"); //$NON-NLS-1$
		zSlotTf.setColumns(10);
		
		rotSlotTf = new JTextField();
		locationPanel.add(rotSlotTf, "10, 4, left, center"); //$NON-NLS-1$
		rotSlotTf.setColumns(10);

		slotLocationPanel = new LocationButtonsPanel(xSlotTf, ySlotTf, zSlotTf, rotSlotTf);
		locationPanel.add(slotLocationPanel, "12, 4, left, top"); //$NON-NLS-1$
		
		JLabel offsetLabel = new JLabel(Translations.getString("FeederConfigurationWizard.LocationPanel.offsetLabel.text")); //$NON-NLS-1$
		locationPanel.add(offsetLabel, "2, 6, right, center"); //$NON-NLS-1$
		
		xOffsetTf = new JTextField();
		locationPanel.add(xOffsetTf, "4, 6, left, center"); //$NON-NLS-1$
		xOffsetTf.setColumns(10);
		
		yOffsetTf = new JTextField();
		locationPanel.add(yOffsetTf, "6, 6, left, center"); //$NON-NLS-1$
		yOffsetTf.setColumns(10);
		
		zOffsetTf = new JTextField();
		locationPanel.add(zOffsetTf, "8, 6, left, center"); //$NON-NLS-1$
		zOffsetTf.setColumns(10);
		
		rotOffsetTf = new JTextField();
		locationPanel.add(rotOffsetTf, "10, 6, left, center"); //$NON-NLS-1$
		rotOffsetTf.setColumns(10);

		offsetLocationPanel = new LocationButtonsPanel(xOffsetTf, yOffsetTf, zOffsetTf, rotOffsetTf);
		locationPanel.add(offsetLocationPanel, "12, 6, left, top"); //$NON-NLS-1$

		moveWhileFeedingLabel = new JLabel(Translations.getString("FeederConfigurationWizard.LocationPanel.moveWhileFeedingLabel.text"));
		locationPanel.add(moveWhileFeedingLabel, "2, 8, right, default"); //$NON-NLS-1$
		moveWhileFeedingLabel.setToolTipText(Translations.getString("FeederConfigurationWizard.LocationPanel.moveWhileFeedingLabel.toolTipText"));

		moveWhileFeedingCheckBox = new JCheckBox();
		locationPanel.add(moveWhileFeedingCheckBox, "4, 8, left, default"); //$NON-NLS-1$
		moveWhileFeedingCheckBox.setToolTipText(Translations.getString("FeederConfigurationWizard.LocationPanel.moveWhileFeedingLabel.toolTipText"));
	}

	AutoBinding<PhotonFeeder, Object, SlotProxy, Object> binding;
	
	@Override
	public void dispose() {
	    super.dispose();
	    binding.unbind();
	}
	
	@Override
	public void createBindings() {
		LengthConverter lengthConverter = new LengthConverter();
		IntegerConverter intConverter = new IntegerConverter();
		DoubleConverter doubleConverter =
				new DoubleConverter(Configuration.get().getLengthDisplayFormat());

		SlotProxy slotProxy = new SlotProxy();
		binding = Bindings.createAutoBinding(UpdateStrategy.READ,
				feeder, BeanProperty.create("slot"), //$NON-NLS-1$
				slotProxy, BeanProperty.create("slot")); //$NON-NLS-1$
		binding.setSourceNullValue(null);
		binding.bind();
//		bind(UpdateStrategy.READ, feeder, "slot", slotProxy, "slot");

		addWrappedBinding(feeder, "hardwareId", hardwareIdValue, "text"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "slotAddress", slotAddressValue, "text"); //$NON-NLS-1$ //$NON-NLS-2$

		addWrappedBinding(feeder, "part", partCb, "selectedItem"); //$NON-NLS-1$ //$NON-NLS-2$
		addWrappedBinding(feeder, "partPitch", partPitchTf, "text", intConverter); //$NON-NLS-1$ //$NON-NLS-2$
		addWrappedBinding(feeder, "feedRetryCount", feedRetryCountTf, "text", intConverter); //$NON-NLS-1$ //$NON-NLS-2$
		addWrappedBinding(feeder, "pickRetryCount", pickRetryCountTf, "text", intConverter); //$NON-NLS-1$ //$NON-NLS-2$
		addWrappedBinding(feeder, "visionEnabled", useVisionCheckbox, "selected");
		addWrappedBinding(feeder, "partVisionEnabled", usePartVisionCheckbox, "selected");
		addWrappedBinding(feeder, "varianceHistory", varianceHistoryTf, "text", intConverter);
		addWrappedBinding(feeder, "skippedCalibration", skippedCalibrationTf, "text", intConverter);

		bind(UpdateStrategy.READ, slotProxy, "enabled", feedAction, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", feedOneMmAction, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$

		bind(UpdateStrategy.READ, slotProxy, "enabled", xSlotTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", ySlotTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", zSlotTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", rotSlotTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", slotLocationPanel, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$

		bind(UpdateStrategy.READ, slotProxy, "enabled", xOffsetTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", yOffsetTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", zOffsetTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", rotOffsetTf, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(UpdateStrategy.READ, slotProxy, "enabled", offsetLocationPanel, "enabled"); //$NON-NLS-1$ //$NON-NLS-2$

		MutableLocationProxy pickLocation = new MutableLocationProxy();
		bind(AutoBinding.UpdateStrategy.READ_WRITE, slotProxy, "location", pickLocation, "location"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, pickLocation, "lengthX", xSlotTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, pickLocation, "lengthY", ySlotTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, pickLocation, "lengthZ", zSlotTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, pickLocation, "rotation", rotSlotTf, "text", doubleConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ, pickLocation, "location", offsetLocationPanel, "baseLocation"); //$NON-NLS-1$ //$NON-NLS-2$

		MutableLocationProxy offsets = new MutableLocationProxy();
		addWrappedBinding(feeder, "offset", offsets, "location"); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, offsets, "lengthX", xOffsetTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, offsets, "lengthY", yOffsetTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, offsets, "lengthZ", zOffsetTf, "text", lengthConverter); //$NON-NLS-1$ //$NON-NLS-2$
		bind(AutoBinding.UpdateStrategy.READ_WRITE, offsets, "rotation", rotOffsetTf, "text", doubleConverter); //$NON-NLS-1$ //$NON-NLS-2$

		addWrappedBinding(feeder, "moveWhileFeeding", moveWhileFeedingCheckBox, "selected"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private final Action findSlotAddressAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.FindSlotAddressAction.Name")) { //$NON-NLS-1$
		@Override
		public void actionPerformed(ActionEvent e) {
			UiUtils.submitUiMachineTask(feeder::findSlotAddress);
		}
	};

	private final Action feedAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.FeedAction.Name")) { //$NON-NLS-1$
		@Override
		public void actionPerformed(ActionEvent e) {
			UiUtils.submitUiMachineTask(() -> {
				final Nozzle nozzle = MainFrame.get().getMachineControls().getSelectedNozzle();
				feeder.feed(nozzle);
			});
		}
	};

	private final Action feedOneMmAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.FeedOneMmAction.Name")) { //$NON-NLS-1$
		@Override
		public void actionPerformed(ActionEvent e) {
			UiUtils.submitUiMachineTask(() -> {
				feeder.feedOneMm();
			});
		}
	};

	private Action positionCameraAction = new AbstractAction(
		Translations.getString("LocalButtonsPanel.Action.PositionCamera"),
		Icons.centerCamera
	) {
		{
			putValue(
				Action.SHORT_DESCRIPTION,
				Translations.getString("LocalButtonsPanel.Action.PositionCamera.Description")
			);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			UiUtils.submitUiMachineTask(() -> {
				// first, use vision to find the offset
				feeder.findSlotAddressIfNeeded();
				feeder.initializeIfNeeded();
				feeder.updateVisionOffsets(null);

				final Camera camera = Configuration.get()
					.getMachine()
					.getDefaultHead()
					.getDefaultCamera();
				MovableUtils.moveToLocationAtSafeZ(camera, feeder.getPickLocation());
				MovableUtils.fireTargetedUserAction(camera);

				Map<String, Object> globals = new HashMap<>();
				globals.put("camera", camera);
				Configuration.get().getScripting().on("Camera.AfterPosition", globals);
			});
		}
	};

	private final Action editPipelineAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.VisionPanel.editPipelineButton.text")) {
		@Override
		public void actionPerformed(final ActionEvent e) {
			UiUtils.messageBoxOnException(() -> {
				final Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
				final CvPipeline pipeline = getCvPipeline(camera, false);
				final CvPipelineEditor editor = new CvPipelineEditor(pipeline);
				final JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), feeder.getName() + " Pipeline", editor);
				dialog.setVisible(true);
			});
		}
	};

	private final Action resetPipelineAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.VisionPanel.resetPipelineButton.text")) {
		@Override
		public void actionPerformed(final ActionEvent e) {
			feeder.resetPipeline();
		}
	};

	private final Action editPartPipelineAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.VisionPanel.editPartPipelineAction.text")) {
		@Override
		public void actionPerformed(final ActionEvent e) {
			UiUtils.messageBoxOnException(() -> {
				final Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
				final CvPipeline partPipeline = getCvPartPipeline(camera, false);
				final CvPipelineEditor editor = new CvPipelineEditor(partPipeline);
				final JDialog dialog = new CvPipelineEditorDialog(MainFrame.get(), feeder.getName() + " Part Pipeline", editor);
				dialog.setVisible(true);
			});
		}
	};

	private final Action resetPartPipelineAction = new AbstractAction(Translations.getString("FeederConfigurationWizard.VisionPanel.resetPartPipelineAction.text")) {
		@Override
		public void actionPerformed(final ActionEvent e) {
			feeder.resetPartPipeline();
		}
	};

	private CvPipeline getCvPipeline(final Camera camera, final boolean clone) {
		final Integer pxMaxDistance = (int) VisionUtils.toPixels(feeder.getHolePitch(), camera);
		final Integer pxMinDiameter = (int) VisionUtils.toPixels(feeder.getHoleDiameterMin(), camera);
		final Integer pxMaxDiameter = (int) VisionUtils.toPixels(feeder.getHoleDiameterMax(), camera);

		try {
			CvPipeline pipeline = feeder.getPipeline();
			if (clone) {
				pipeline = pipeline.clone();
			}
			pipeline.setProperty("camera", camera);
			pipeline.setProperty("feeder", feeder);
			pipeline.setProperty("DetectCircularSymmetry.maxDistance", pxMaxDistance / 2);
			pipeline.setProperty("DetectCircularSymmetry.minDiameter", pxMinDiameter);
			pipeline.setProperty("DetectCircularSymmetry.maxDiameter", pxMaxDiameter);
			pipeline.setProperty("DetectCircularSymmetry.searchHeight", pxMaxDistance);
			pipeline.setProperty("DetectCircularSymmetry.searchWidth", pxMinDiameter / 2);
			return pipeline;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	private CvPipeline getCvPartPipeline(final Camera camera, final boolean clone) {
		try {
			CvPipeline pipeline = feeder.getPartPipeline();
			if (clone) {
				pipeline = pipeline.clone();
			}
			pipeline.setProperty("camera", camera);
			pipeline.setProperty("feeder", feeder);
			return pipeline;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
}
